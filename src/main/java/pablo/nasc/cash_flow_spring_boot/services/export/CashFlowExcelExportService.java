package pablo.nasc.cash_flow_spring_boot.services.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pablo.nasc.cash_flow_spring_boot.entities.Debt;
import pablo.nasc.cash_flow_spring_boot.entities.Installment;
import pablo.nasc.cash_flow_spring_boot.entities.Tag;
import pablo.nasc.cash_flow_spring_boot.repositories.DebtRepository;
import pablo.nasc.cash_flow_spring_boot.repositories.InstallmentRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class CashFlowExcelExportService {

    private final DebtRepository debtRepository;
    private final InstallmentRepository installmentRepository;

    @Transactional(readOnly = true)
    public byte[] exportUserCashFlow(Long userId) {
        List<Debt> debts = debtRepository.findAllByUserIdForExport(userId);
        List<Installment> installments = installmentRepository.findAllByUserIdForExport(userId);

        return createWorkbook(
                new Sheet("Dividas", debtRows(debts)),
                new Sheet("Parcelas", installmentRows(installments))
        );
    }

    private List<List<Cell>> debtRows(List<Debt> debts) {
        List<List<Cell>> rows = new ArrayList<>();
        rows.add(strings(
                "ID",
                "Titulo",
                "Descricao",
                "Credor",
                "Categoria",
                "Valor total",
                "Total de parcelas",
                "Data inicial",
                "Taxa de juros",
                "Ativa",
                "Tags",
                "Criado em"
        ));

        for (Debt debt : debts) {
            rows.add(List.of(
                    number(debt.getId()),
                    text(debt.getTitle()),
                    text(debt.getDescription()),
                    text(debt.getCreditor()),
                    text(debt.getCategory() == null ? null : debt.getCategory().getName()),
                    number(debt.getTotalAmount()),
                    number(debt.getTotalInstallments()),
                    text(debt.getStartDate()),
                    number(debt.getInterestRate()),
                    text(Boolean.TRUE.equals(debt.getActive()) ? "Sim" : "Nao"),
                    text(debt.getTags().stream().map(Tag::getName).filter(Objects::nonNull).toList()),
                    text(debt.getCreatedAt())
            ));
        }

        return rows;
    }

    private List<List<Cell>> installmentRows(List<Installment> installments) {
        List<List<Cell>> rows = new ArrayList<>();
        rows.add(strings(
                "ID",
                "Divida ID",
                "Divida",
                "Categoria",
                "Numero",
                "Valor",
                "Vencimento",
                "Pagamento",
                "Status",
                "Observacoes"
        ));

        for (Installment installment : installments) {
            Debt debt = installment.getDebt();

            rows.add(List.of(
                    number(installment.getId()),
                    number(debt == null ? null : debt.getId()),
                    text(debt == null ? null : debt.getTitle()),
                    text(debt == null || debt.getCategory() == null ? null : debt.getCategory().getName()),
                    number(installment.getInstallmentNumber()),
                    number(installment.getAmount()),
                    text(installment.getDueDate()),
                    text(installment.getPaymentDate()),
                    text(installment.getStatus() == null ? null : installment.getStatus().name()),
                    text(installment.getNotes())
            ));
        }

        return rows;
    }

    private byte[] createWorkbook(Sheet... sheets) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(outputStream, StandardCharsets.UTF_8)) {

            add(zip, "[Content_Types].xml", contentTypes(sheets.length));
            add(zip, "_rels/.rels", rootRelationships());
            add(zip, "docProps/app.xml", appProperties());
            add(zip, "docProps/core.xml", coreProperties());
            add(zip, "xl/workbook.xml", workbook(sheets));
            add(zip, "xl/_rels/workbook.xml.rels", workbookRelationships(sheets.length));
            add(zip, "xl/styles.xml", styles());

            for (int index = 0; index < sheets.length; index++) {
                add(zip, "xl/worksheets/sheet" + (index + 1) + ".xml", worksheet(sheets[index]));
            }

            zip.finish();
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Nao foi possivel gerar o arquivo Excel.", ex);
        }
    }

    private void add(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String contentTypes(int sheetCount) {
        StringBuilder xml = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                <Default Extension="xml" ContentType="application/xml"/>
                <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
                <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
                <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
                """);

        for (int index = 1; index <= sheetCount; index++) {
            xml.append("<Override PartName=\"/xl/worksheets/sheet")
                    .append(index)
                    .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        }

        return xml.append("</Types>").toString();
    }

    private String rootRelationships() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
                <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
                </Relationships>
                """;
    }

    private String appProperties() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
                <Application>Cash Flow Spring Boot</Application>
                </Properties>
                """;
    }

    private String coreProperties() {
        String now = Instant.now().toString();
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                <dc:creator>Cash Flow API</dc:creator>
                <cp:lastModifiedBy>Cash Flow API</cp:lastModifiedBy>
                <dcterms:created xsi:type="dcterms:W3CDTF">%s</dcterms:created>
                <dcterms:modified xsi:type="dcterms:W3CDTF">%s</dcterms:modified>
                </cp:coreProperties>
                """.formatted(now, now);
    }

    private String workbook(Sheet[] sheets) {
        StringBuilder xml = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                <sheets>
                """);

        for (int index = 0; index < sheets.length; index++) {
            int sheetNumber = index + 1;
            xml.append("<sheet name=\"")
                    .append(escape(sheets[index].name()))
                    .append("\" sheetId=\"")
                    .append(sheetNumber)
                    .append("\" r:id=\"rId")
                    .append(sheetNumber)
                    .append("\"/>");
        }

        return xml.append("</sheets></workbook>").toString();
    }

    private String workbookRelationships(int sheetCount) {
        StringBuilder xml = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                """);

        for (int index = 1; index <= sheetCount; index++) {
            xml.append("<Relationship Id=\"rId")
                    .append(index)
                    .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet")
                    .append(index)
                    .append(".xml\"/>");
        }

        xml.append("<Relationship Id=\"rId")
                .append(sheetCount + 1)
                .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>");

        return xml.append("</Relationships>").toString();
    }

    private String styles() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                <fonts count="2"><font><sz val="11"/><name val="Calibri"/></font><font><b/><sz val="11"/><name val="Calibri"/></font></fonts>
                <fills count="2"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill></fills>
                <borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
                <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
                <cellXfs count="2"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/><xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/></cellXfs>
                </styleSheet>
                """;
    }

    private String worksheet(Sheet sheet) {
        StringBuilder xml = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                <sheetViews><sheetView workbookViewId="0"><pane ySplit="1" topLeftCell="A2" activePane="bottomLeft" state="frozen"/></sheetView></sheetViews>
                <sheetData>
                """);

        for (int rowIndex = 0; rowIndex < sheet.rows().size(); rowIndex++) {
            int rowNumber = rowIndex + 1;
            List<Cell> row = sheet.rows().get(rowIndex);
            xml.append("<row r=\"").append(rowNumber).append("\">");

            for (int columnIndex = 0; columnIndex < row.size(); columnIndex++) {
                appendCell(xml, row.get(columnIndex), rowNumber, columnIndex + 1, rowIndex == 0);
            }

            xml.append("</row>");
        }

        return xml.append("</sheetData><pageMargins left=\"0.7\" right=\"0.7\" top=\"0.75\" bottom=\"0.75\" header=\"0.3\" footer=\"0.3\"/></worksheet>").toString();
    }

    private void appendCell(StringBuilder xml,
                            Cell cell,
                            int rowNumber,
                            int columnNumber,
                            boolean header) {
        String reference = columnName(columnNumber) + rowNumber;
        String style = header ? " s=\"1\"" : "";

        if (cell.type() == CellType.NUMBER && !cell.value().isBlank()) {
            xml.append("<c r=\"")
                    .append(reference)
                    .append("\"")
                    .append(style)
                    .append("><v>")
                    .append(cell.value())
                    .append("</v></c>");
            return;
        }

        xml.append("<c r=\"")
                .append(reference)
                .append("\" t=\"inlineStr\"")
                .append(style)
                .append("><is><t xml:space=\"preserve\">")
                .append(escape(cell.value()))
                .append("</t></is></c>");
    }

    private String columnName(int columnNumber) {
        StringBuilder column = new StringBuilder();
        int value = columnNumber;

        while (value > 0) {
            value--;
            column.insert(0, (char) ('A' + (value % 26)));
            value /= 26;
        }

        return column.toString();
    }

    private List<Cell> strings(String... values) {
        List<Cell> cells = new ArrayList<>();

        for (String value : values) {
            cells.add(text(value));
        }

        return cells;
    }

    private Cell text(Object value) {
        if (value instanceof LocalDate date) {
            return new Cell(date.toString(), CellType.STRING);
        }

        if (value instanceof LocalDateTime dateTime) {
            return new Cell(dateTime.toString(), CellType.STRING);
        }

        if (value instanceof List<?> values) {
            return new Cell(String.join(", ", values.stream().map(String::valueOf).toList()), CellType.STRING);
        }

        return new Cell(value == null ? "" : String.valueOf(value), CellType.STRING);
    }

    private Cell number(Number value) {
        if (value == null) {
            return text("");
        }

        if (value instanceof BigDecimal decimal) {
            return new Cell(decimal.toPlainString(), CellType.NUMBER);
        }

        return new Cell(String.valueOf(value), CellType.NUMBER);
    }

    private String escape(String value) {
        return sanitize(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String sanitize(String value) {
        StringBuilder sanitized = new StringBuilder();

        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current == 0x9 || current == 0xA || current == 0xD
                    || (current >= 0x20 && current <= 0xD7FF)
                    || (current >= 0xE000 && current <= 0xFFFD)) {
                sanitized.append(current);
            } else {
                sanitized.append(' ');
            }
        }

        return sanitized.toString();
    }

    private enum CellType {
        STRING,
        NUMBER
    }

    private record Sheet(String name, List<List<Cell>> rows) {
    }

    private record Cell(String value, CellType type) {
    }
}
