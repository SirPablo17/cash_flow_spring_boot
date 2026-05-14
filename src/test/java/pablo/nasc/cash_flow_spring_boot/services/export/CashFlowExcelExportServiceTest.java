package pablo.nasc.cash_flow_spring_boot.services.export;

import org.junit.jupiter.api.Test;
import pablo.nasc.cash_flow_spring_boot.repositories.DebtRepository;
import pablo.nasc.cash_flow_spring_boot.repositories.InstallmentRepository;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CashFlowExcelExportServiceTest {

    @Test
    void generatesValidXlsxPackageWithExpectedSheets() throws Exception {
        DebtRepository debtRepository = mock(DebtRepository.class);
        InstallmentRepository installmentRepository = mock(InstallmentRepository.class);
        CashFlowExcelExportService service =
                new CashFlowExcelExportService(debtRepository, installmentRepository);

        when(debtRepository.findAllByUserIdForExport(1L)).thenReturn(List.of());
        when(installmentRepository.findAllByUserIdForExport(1L)).thenReturn(List.of());

        byte[] workbook = service.exportUserCashFlow(1L);

        assertThat(workbook).startsWith(new byte[] {'P', 'K'});
        assertThat(entries(workbook)).contains(
                "[Content_Types].xml",
                "xl/workbook.xml",
                "xl/worksheets/sheet1.xml",
                "xl/worksheets/sheet2.xml",
                "xl/styles.xml"
        );
    }

    private Set<String> entries(byte[] workbook) throws Exception {
        Set<String> entries = new HashSet<>();

        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(workbook))) {
            var entry = zip.getNextEntry();

            while (entry != null) {
                entries.add(entry.getName());
                entry = zip.getNextEntry();
            }
        }

        return entries;
    }
}
