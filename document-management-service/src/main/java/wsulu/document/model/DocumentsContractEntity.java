package wsulu.document.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * An entity that includes all documents, all versions of a particular loan.
 */
@Entity(name = "documents_contract")
@Getter
@Setter
@NoArgsConstructor
public class DocumentsContractEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "loan_id")
    private Long loanId;

    @Column(name = "contract_number")
    private String contractNumber;

    @Column(name = "mobile")
    private String mobile;

    @OneToMany(
            mappedBy = "documentsContract",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true)
    private Set<DocumentEntity> allDocuments;
}
