package wsulu.document.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import wsulu.document.model.TemplateEntity;
import wsulu.document.repo.TemplateRepo;
import wsulu.document.service.TemplateService;

import java.util.Date;

/**
 * Service class for working with document templates in .docx format
 */
@Service
@SuppressWarnings("unused")
public class TemplateServiceImpl implements TemplateService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateServiceImpl.class);
    private final TemplateRepo templateRepo;

    @Autowired
    public TemplateServiceImpl(TemplateRepo templateRepo) {
        this.templateRepo = templateRepo;
    }

    @Override
    public boolean saveTemplateDocx(MultipartFile file, String templateName, String userName) {
        try {
            byte[] bodyDocument = file.getBytes();
            TemplateEntity currentDocumentInRepo = templateRepo.findFirstByTitleOrderByIdDesc(templateName);
            long version = currentDocumentInRepo == null ? 1L : currentDocumentInRepo.getVersion() + 1;
            TemplateEntity newDocumentInRepo = new TemplateEntity();
            newDocumentInRepo.setTitle(templateName);
            newDocumentInRepo.setDocumentBody(bodyDocument);
            newDocumentInRepo.setVersion(version);
            newDocumentInRepo.setCreateDate(new Date());
            newDocumentInRepo.setUserName(userName);
            templateRepo.save(newDocumentInRepo);
        } catch (Exception e) {
            logger.error("save: error={}", e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public byte[] getTemplateById(Long templateId) {
        try {
            TemplateEntity currentDocumentInRepo = templateRepo.getReferenceById(templateId);
            return currentDocumentInRepo.getDocumentBody();
        } catch (Exception e) {
            logger.error("getTemplateById: error={}", e.getMessage());
            return new byte[0];
        }
    }

    @Override
    public byte[] getLastTemplateBodyByTitle(String title) {
        try {
            TemplateEntity currentDocumentInRepo = templateRepo.findFirstByTitleOrderByIdDesc(title);
            return currentDocumentInRepo == null ? new byte[0] : currentDocumentInRepo.getDocumentBody();
        } catch (Exception e) {
            logger.error("getLastTemplateByTitle: error={}", e.getMessage(), e);
            return new byte[0];
        }
    }
}
