package com.upg.legacyhr.soap;

import com.upg.legacyhr.model.Employee;
import com.upg.legacyhr.repository.EmployeeRepository;
import com.upg.legacyhr.soap.generated.GetAllDepartmentsRequest;
import com.upg.legacyhr.soap.generated.GetAllDepartmentsResponse;
import com.upg.legacyhr.soap.generated.GetDepartmentEmployeesRequest;
import com.upg.legacyhr.soap.generated.GetDepartmentEmployeesResponse;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

/**
 * Endpoint SOAP du systeme RH legacy.
 *
 * <p>Declare avec {@code @Endpoint}, ce bean est publie dans un
 * {@code MessageDispatcherServlet} et traite les operations du contrat
 * {@code http://upg.ac.rw/soap/hr}.</p>
 *
 * <p>L'annotation {@code @PayloadRoot} assure le routage : Spring-WS lit le
 * QName de l'element racine de la trame SOAP et invoque la methode dont le
 * couple (namespace, localPart) correspond. Aucune table de routage
 * supplementaire n'est necessaire.</p>
 *
 * <p>Point cle pour le client : toute {@link HrServiceFault} levee ici est
 * automatiquement enveloppee dans une balise {@code <soap:Fault>} par le
 * resoluteur declare dans {@code WebServiceConfig}. Le client Node.js doit
 * savoir intercepter cette erreur.</p>
 */
@Endpoint
@Component
public class HrServiceEndpoint {

    /** Namespace cible du contrat : doit correspondre au targetNamespace du XSD. */
    static final String NAMESPACE = "http://upg.ac.rw/soap/hr";

    private final EmployeeRepository employeeRepository;
    private final EmployeeSoapMapper mapper;

    public HrServiceEndpoint(EmployeeRepository employeeRepository, EmployeeSoapMapper mapper) {
        this.employeeRepository = employeeRepository;
        this.mapper = mapper;
    }

    /**
     * Operation principale : lister les employes d'un departement avec leurs
     * competences imbriquees.
     *
     * <p>Correspond a la trame :</p>
     * <pre>
     * &lt;getDepartmentEmployeesRequest&gt;
     *   &lt;department&gt;IT&lt;/department&gt;
     * &lt;/getDepartmentEmployeesRequest&gt;
     * </pre>
     *
     * <p>Le `localPart` vaut <b>{@code getDepartmentEmployeesRequest}</b> et
     * non {@code getDepartmentEmployees} : Spring-WS route sur le nom LOCAL
     * de l'element racine de la trame, et l'element declare dans le XSD porte
     * le suffixe "Request" (convention imposee par le generateur de WSDL,
     * voir le commentaire du XSD). C'est ce meme nom que le client SOAP
     * appelle, l'operation restant {@code getDepartmentEmployees}.</p>
     *
     * @param request requete JAXB liee au corps de la trame
     * @return reponse JAXB serialisee en XML
     */
    @PayloadRoot(namespace = NAMESPACE, localPart = "getDepartmentEmployeesRequest")
    public @ResponsePayload GetDepartmentEmployeesResponse getDepartmentEmployees(
            @RequestPayload GetDepartmentEmployeesRequest request) {

        // 1. Controle de la requete
        String departement = request == null ? null : request.getDepartment();

        if (departement == null || departement.isBlank()) {
            throw new InvalidRequestException(
                    "Le parametre 'department' est obligatoire et ne peut pas etre vide.");
        }

        String departementNormalise = departement.trim();

        // 2. Regle metier : un departement inconnu est une ERREUR, pas une
        //    liste vide. Cela produit un <soap:Fault> exploitable par le client.
        if (!employeeRepository.existsByDepartmentIgnoreCase(departementNormalise)) {
            throw new DepartmentNotFoundException(departementNormalise);
        }

        // 3. Requete SQL avec JOIN FETCH des competences (une seule requete,
        //    pas de N+1)
        List<Employee> employes = employeeRepository.findByDepartmentWithSkills(departementNormalise);

        // 4. Mapping JPA -> JAXB (construction de la liste imbriquee)
        GetDepartmentEmployeesResponse response = new GetDepartmentEmployeesResponse();
        response.setEmployees(mapper.toSoapEmployeeList(employes));
        response.setTotalCount(employes.size());

        return response;
    }

    /**
     * Operation d'appui : lister les departements et leur effectif.
     *
     * <p>Permet au client Node.js d'alimenter un menu deroulant sans avoir a
     * deviner les codes de departement.</p>
     */
    @PayloadRoot(namespace = NAMESPACE, localPart = "getAllDepartmentsRequest")
    public @ResponsePayload GetAllDepartmentsResponse getAllDepartments(
            @RequestPayload GetAllDepartmentsRequest request) {

        GetAllDepartmentsResponse response = new GetAllDepartmentsResponse();
        response.setDepartments(mapper.toSoapDepartmentList(employeeRepository.countEmployeesByDepartment()));

        return response;
    }
}
