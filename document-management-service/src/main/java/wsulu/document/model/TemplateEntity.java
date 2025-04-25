package wsulu.document.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * An entity to store a document template in the database as a byte array, in .docx format, with versioning maintained.
 */
@Entity(name = "document_template")
@Getter
@Setter
@NoArgsConstructor
public class TemplateEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "document_body")
    private byte[] documentBody;

    @Column(name = "version")
    private Long version;

    @Column(name = "create_date")
    private Date createDate;

    @Column(name = "user_name")
    private String userName;
}
