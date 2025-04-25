package wsulu.document.service;

import org.springframework.web.multipart.MultipartFile;

public interface TemplateService {

    /**
     * Saving a new template in the base, with versioning respected
     *
     * @param file document template in .docx format
     * @param templateName template name
     * @param userName user name
     * @return boolean - the result of an attempt to save the template in the database
     */
    boolean saveTemplateDocx(MultipartFile file, String templateName, String userName);

    /**
     * Retrieve the saved template from the database as a byte array in .docx format. Search by
     * specific template id
     *
     * @param templateId template name
     * @return template as a byte array in .docx format
     */
    byte[] getTemplateById(Long templateId);

    /**
     * Retrieve the latest version of a saved template from the database as a byte array in .docx format.
     * Search by template name
     *
     * @param title template name
     * @return template as a byte array in .docx format
     */
    byte[] getLastTemplateBodyByTitle(String title);
}
