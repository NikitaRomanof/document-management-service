package wsulu.document.utils;

import fr.opensagres.odfdom.converter.pdf.PdfOptions;
import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.ConverterTypeVia;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * Утилитный класс для конвертации .docx документа в .pdf документ с подставлением переменных из
 * мапы
 */
public class ConverterUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConverterUtil.class);

    public static byte[] generatePdf(Map<String, Object> context, byte[] documentBody) {

        try (InputStream in = new ByteArrayInputStream(documentBody);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            IXDocReport report =
                    XDocReportRegistry.getRegistry().loadReport(in, TemplateEngineKind.Velocity);
            Options options =
                    Options.getTo(ConverterTypeTo.PDF)
                            .via(ConverterTypeVia.XWPF)
                            .subOptions(PdfOptions.create().fontEncoding("windows-1250"));
            report.convert(context, options, out);
            return out.toByteArray();
        } catch (Exception e) {
            logger.error("generatePdf: error={}", e.getMessage());
            return null;
        }
    }
}
