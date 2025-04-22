package dtn.ServiceScore.components;

import dtn.ServiceScore.dtos.ClassDTO;
import dtn.ServiceScore.dtos.UserDTO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class ExcelHelper {
//    public List<UserDTO> excelToUsers(MultipartFile file) throws IOException {
//        List<UserDTO> users = new ArrayList<>();
//
//        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
//            Sheet sheet = workbook.getSheetAt(0);
//
//            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Bỏ qua header
//                Row row = sheet.getRow(i);
//                if (row == null) continue;
//
//                UserDTO user = new UserDTO();
//                user.setFullname(row.getCell(0).getStringCellValue());
//                user.setPhoneNumber(row.getCell(1).getStringCellValue());
//                user.setStudentId(row.getCell(2).getStringCellValue());
//                user.setAddress(row.getCell(3).getStringCellValue());
//                user.setDateOfBirth(LocalDate.parse(row.getCell(4).getStringCellValue())); // yyyy-MM-dd
//                user.setEmail(row.getCell(5).getStringCellValue());
//                user.setUsername(row.getCell(6).getStringCellValue());
//                user.setPassword(row.getCell(7).getStringCellValue());
//                user.setRetypePassword(row.getCell(8).getStringCellValue());
//                user.setClassName(row.getCell(9).getStringCellValue());
//                user.setRoleName(row.getCell(10).getStringCellValue());
//
//                users.add(user);
//            }
//        }
//
//        return users;
//    }

    public List<ClassDTO> excelToClasses(MultipartFile file) throws IOException {
        List<ClassDTO> classes = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Bỏ qua header
                Row row = sheet.getRow(i);
                if (row == null) continue;

                ClassDTO classDTO = new ClassDTO();
                classDTO.setName(row.getCell(0).getStringCellValue());
                classDTO.setDepartmentId((long) row.getCell(1).getNumericCellValue());
                classDTO.setCourse(row.getCell(2).getStringCellValue());
                classDTO.setStatus(row.getCell(3).getBooleanCellValue());

                classes.add(classDTO);
            }
        }

        return classes;
    }
    public List<UserDTO> excelToUsers(MultipartFile file) throws IOException {
        List<UserDTO> users = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Bỏ qua dòng header
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    UserDTO user = new UserDTO();
                    user.setFullname(getCellValueAsString(row.getCell(0)));
                    user.setPhoneNumber(getCellValueAsString(row.getCell(1)));
                    user.setStudentId(getCellValueAsString(row.getCell(2)));
                    user.setAddress(getCellValueAsString(row.getCell(3)));
                    user.setDateOfBirth(LocalDate.parse(getCellValueAsString(row.getCell(4))));
                    user.setEmail(getCellValueAsString(row.getCell(5)));
                    user.setUsername(getCellValueAsString(row.getCell(6)));
                    user.setPassword(getCellValueAsString(row.getCell(7)));
                    user.setRetypePassword(getCellValueAsString(row.getCell(8)));
                    user.setClassName(getCellValueAsString(row.getCell(9)));
                    user.setRoleName(getCellValueAsString(row.getCell(10)));

                    users.add(user);
                } catch (Exception e) {
                    throw new IOException("Lỗi tại dòng " + (i + 1) + ": " + e.getMessage(), e);
                }
            }
        }

        return users;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString(); // yyyy-MM-dd
                }
                return BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
            default:
                return "";
        }
    }

}
