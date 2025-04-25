package wsulu.document.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wsulu.document.model.DocumentEntity;
import wsulu.document.model.DocumentsContractEntity;
import wsulu.document.repo.DocumentsContractRepo;
import wsulu.document.service.DocumentZipService;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The class is responsible for creating a .zip archive containing individual documents in .pdf format
 */
@Service
public class DocumentZipServiceImpl implements DocumentZipService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentZipServiceImpl.class);
    private final DocumentsContractRepo documentsContractRepo;

    @Autowired
    public DocumentZipServiceImpl(DocumentsContractRepo documentsContractRepo) {
        this.documentsContractRepo = documentsContractRepo;
    }

    @Override
    public byte[] getAllDocumentByLoanId(Long loanId, Long clientId, boolean allVersion) {
        byte[] result = new byte[0];
        try (ByteArrayOutputStream resultOut = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(resultOut)) {
            Set<DocumentEntity> allDocument =
                    allVersion
                            ? getAllVersionDocumentByLoanId(loanId, clientId)
                            : getLastVersionDocumentByLoanId(loanId, clientId);
            allDocument.forEach(
                    e -> {
                        try {
                            byte[] bodyPdf = e.getContractPdf();
                            ZipEntry zipEntry =
                                    new ZipEntry(e.getDocumentTitle() + "_v." + e.getVersion() + ".pdf");
                            zipOut.putNextEntry(zipEntry);
                            zipOut.write(bodyPdf, 0, bodyPdf.length);
                            zipOut.closeEntry();
                        } catch (Exception loopException) {
                            logger.error(
                                    "getAllDocumentByLoanId: error in the loop, error={}",
                                    loopException.getMessage());
                        }
                    });

            zipOut.finish();
            resultOut.flush();
            result = resultOut.toByteArray();
        } catch (Exception e) {
            logger.error("getAllDocumentByLoanId: error={}", e.getMessage());
        }
        return result;
    }

    private Set<DocumentEntity> getAllVersionDocumentByLoanId(Long loanId, Long clientId) {
        DocumentsContractEntity documents = documentsContractRepo.findFirstByLoanId(loanId, clientId);
        return documents.getAllDocuments();
    }

    private Set<DocumentEntity> getLastVersionDocumentByLoanId(Long loanId, Long clientId) {
        DocumentsContractEntity documents = documentsContractRepo.findFirstByLoanId(loanId, clientId);
        Map<String, DocumentEntity> bufferMap = new TreeMap<>();
        documents
                .getAllDocuments()
                .forEach(
                        e -> {
                            String title = e.getDocumentTitle();
                            if (bufferMap.containsKey(title)) {
                                if (bufferMap.get(title).getVersion() < e.getVersion()) {
                                    bufferMap.put(title, e);
                                }
                            } else {
                                bufferMap.put(title, e);
                            }
                        });
        return new HashSet<>(bufferMap.values());
    }
}
