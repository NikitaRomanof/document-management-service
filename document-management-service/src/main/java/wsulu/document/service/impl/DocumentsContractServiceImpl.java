package wsulu.document.service.impl;


import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wsulu.document.enums.Keys;
import wsulu.document.model.DocumentEntity;
import wsulu.document.model.DocumentsContractEntity;
import wsulu.document.repo.DocumentsContractRepo;
import wsulu.document.service.DocumentsContractService;
import wsulu.document.service.TemplateService;
import wsulu.document.utils.ConverterUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for working with client documents in .pdf format
 */
@Service
public class DocumentsContractServiceImpl implements DocumentsContractService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentsContractServiceImpl.class);

    private final DocumentsContractRepo documentsContractRepo;
    private final TemplateService templateService;

    @Autowired
    public DocumentsContractServiceImpl(
            DocumentsContractRepo documentsContractRepo, TemplateService templateService) {
        this.documentsContractRepo = documentsContractRepo;
        this.templateService = templateService;
    }

    @Override
    public byte[] getDocumentsByLoan(@NotNull Long loanId,
                                     @NotNull Long clientId,
                                     @NotNull String templateName) {
        DocumentsContractEntity contract = documentsContractRepo.findFirstByLoanId(loanId, clientId);
        if (contract == null) {
            logger.error("getContractByLoan: DocumentsContractEntity is null, clientId={}", clientId);
            return new byte[0];
        }
        DocumentEntity document = findLastVersionByDocumentTitle(contract.getAllDocuments(), templateName);
        if (document == null) {
            logger.error("getContractByLoan: document is null, clientId={}", clientId);
            return new byte[0];
        }
        return document.getContractPdf();
    }

    @Override
    public byte[] previewDocumentsContract(@NotNull Map<String, Object> data) {
        if (!data.containsKey(Keys.TEMPLATE.name().toLowerCase())) {
            logger.warn("reviewDocumentsContract: data not contain template name");
            return null;
        }
        byte[] documentBody = getDocxBodyLastVersion((String)data.get(Keys.TEMPLATE.name().toLowerCase()));
        if (documentBody.length == 0) {
            logger.warn("reviewDocumentsContract: template not found");
            return null;
        }
        return ConverterUtil.generatePdf(data, documentBody);
    }

    @Override
    public boolean generateAndSave(@NotNull List<Map<String, Object>> data) {
        data = data.stream().filter(e -> e.containsKey(Keys.TEMPLATE.name().toLowerCase())).collect(Collectors.toList());
        if (data.isEmpty()) {
            logger.warn("generateAndSave: data not contain template name");
            return false;
        }

        List<Boolean> allGenerateDocs = new ArrayList<>(data.size());
        for (Map<String, Object> tmp : data) {
            byte[] documentBody = getDocxBodyLastVersion((String)tmp.get(Keys.TEMPLATE.name().toLowerCase()));
            if (documentBody.length == 0) {
                logger.warn("generateAndSave: template not found");
                continue;
            }
            byte[] pdfBody = ConverterUtil.generatePdf(tmp, documentBody);
            if (pdfBody == null || pdfBody.length == 0) {
                logger.warn("generateAndSave: error when try generate document by template");
                continue;
            }
            allGenerateDocs.add(saveInnerDocsByTemplate(tmp, pdfBody));
        }
        return !allGenerateDocs.isEmpty();
    }


    private boolean saveInnerDocsByTemplate(@NotNull Map<String, Object> templateData,
                                            @NotNull byte[] pdfBody) {
        try {
            Long clientId = (Long)templateData.get(Keys.CLIENT_ID.name().toLowerCase());
            Long loanId = (Long)templateData.get(Keys.LOAN_ID.name().toLowerCase());
            String mobile = (String)templateData.get(Keys.MOBILE.name().toLowerCase());
            String template = (String)templateData.get(Keys.TEMPLATE.name().toLowerCase());
            if (clientId == null || loanId == null || mobile == null || mobile.isEmpty()) {
                logger.warn("saveInnerDocsByTemplate: any client params in data not found");
                return false;
            }
            DocumentsContractEntity currentDocs =
                    documentsContractRepo.findFirstByLoanId(loanId, clientId);
            if (currentDocs == null) {
                DocumentsContractEntity newDocs = buildNewDocumentsContractEntity(clientId, loanId, mobile);
                documentsContractRepo.save(newDocs);
                HashSet<DocumentEntity> setDocs = new HashSet<>();
                setDocs.add(buildNewDocumentEntity(template, newDocs, 1L, pdfBody));
                newDocs.setAllDocuments(setDocs);
                documentsContractRepo.save(newDocs);
            } else {
                Set<DocumentEntity> setDocs = currentDocs.getAllDocuments();
                setDocs = setDocs == null ? new HashSet<>() : setDocs;
                DocumentEntity loanContractVersion = findLastVersionByDocumentTitle(setDocs, template);
                long version = loanContractVersion == null ? 0L : loanContractVersion.getVersion();
                DocumentEntity documentEntity =
                        buildNewDocumentEntity(template, currentDocs, version + 1, pdfBody);
                setDocs.add(documentEntity);
                documentsContractRepo.save(currentDocs);
            }
            return true;
        } catch (Exception e) {
            logger.error("saveInnerDocsByTitle: error={}", e.getMessage(), e);
            return false;
        }
    }

    private byte[] getDocxBodyLastVersion(@NotNull String templateName) {
        byte[] documentBody = templateService.getLastTemplateBodyByTitle(templateName);
        return documentBody == null ? new byte[0] : documentBody;
    }

    private DocumentEntity buildNewDocumentEntity(String documentTitle,
                                                  DocumentsContractEntity docs,
                                                  Long version,
                                                  byte[] pdfBody) {
        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setDocumentsContract(docs);
        documentEntity.setDocumentTitle(documentTitle);
        documentEntity.setVersion(version);
        documentEntity.setContractPdf(pdfBody);
        documentEntity.setCreateDate(new Date());
        return documentEntity;
    }

    private DocumentsContractEntity buildNewDocumentsContractEntity(Long loanId, Long clientId, String mobile) {
        DocumentsContractEntity newDocs = new DocumentsContractEntity();
        newDocs.setClientId(clientId);
        newDocs.setLoanId(loanId);
        long count = documentsContractRepo.countByMobile(mobile) + 1;
        newDocs.setContractNumber(mobile + "-" + count);
        newDocs.setMobile(mobile);
        return newDocs;
    }

    private DocumentEntity findLastVersionByDocumentTitle(Set<DocumentEntity> documentEntitySet,
                                                          @NotNull String documentTitle) {
        Optional<DocumentEntity> opt =
                documentEntitySet.stream()
                        .filter(e -> documentTitle.equals(e.getDocumentTitle()))
                        .max(Comparator.comparingLong(DocumentEntity::getVersion));
        return opt.orElse(null);
    }
}
