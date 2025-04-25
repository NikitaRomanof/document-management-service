package wsulu.document.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * The entity of a specific .pdf document, a specific version
 */
@Entity(name = "document")
@Getter
@Setter
@NoArgsConstructor
public class DocumentEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_pdf")
    private byte[] contractPdf;

    @Column(name = "version")
    private Long version;

    @Column(name = "document_title")
    private String documentTitle;

    @ManyToOne
    @JoinColumn(name = "documents_contract_id")
    private DocumentsContractEntity documentsContract;

    @Column(name = "create_date")
    private Date createDate;
}
