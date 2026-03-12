package org.example;
import com.spire.xls.*;

import java.awt.*;

public class TestExcel {

    public static void main(String[] args){
        //创建Workbook实例
        Workbook workbook = new Workbook();

        //获取第一张工作表（新建的Workbook默认包含3张工作表）
        Worksheet sheet = workbook.getWorksheets().get(0);
        //为第一张工作表设置名称
        sheet.setName("Data Sheet");

        //创建列头单元格样式
        CellStyle style1 = workbook.getStyles().addStyle("Header Style");
        style1.getFont().setSize(12f);
        style1.getFont().setColor(Color.BLACK);
        style1.getFont().isBold(true);
        style1.setWrapText(true); // 设置自动换行
        style1.setHorizontalAlignment(HorizontalAlignType.Center);
        style1.setVerticalAlignment(VerticalAlignType.Center);

        //创建数据单元格样式
        CellStyle style2 = workbook.getStyles().addStyle("Data Style");
        style2.getFont().setSize(10f);
        style2.getFont().setColor(Color.BLACK);
        style2.setWrapText(true); // 设置自动换行

        //为列头单元格添加数据并应用样式
        for (int column=1; column<5; column++)
        {
            CellRange header =sheet.getCellRange(1,column);
            header.setValue("Column " + column );
            header.setStyle(style1);
            header.setColumnWidth(15f);
        }

        //为数据单元格添加数据并应用样式
        for (int row=2; row<11; row++)
        {
            for (int column=1; column<5; column++)
            {
                CellRange cell = sheet.getCellRange(row, column);
                cell.setValue("Data " + "\n" + row + ", " + column);
                cell.setStyle(style2);
            }
        }

//        //显示网格线
//        sheet.setGridLinesVisible(true);

        //按范围合并单元格
        CellRange cell = sheet.getRange().get("A11:D11");
        cell.merge();

        //分别设置上、下、左、右边框
        cell.getBorders().getByBordersLineType(BordersLineType.EdgeTop).setLineStyle(LineStyleType.Thin);
        cell.getBorders().getByBordersLineType(BordersLineType.EdgeTop).setColor(Color.BLACK);
        cell.getBorders().getByBordersLineType(BordersLineType.EdgeBottom).setLineStyle(LineStyleType.Thin);
        cell.getBorders().getByBordersLineType(BordersLineType.EdgeBottom).setColor(Color.BLACK);
        cell.getBorders().getByBordersLineType(BordersLineType.EdgeLeft).setLineStyle(LineStyleType.Thin);
        cell.getBorders().getByBordersLineType(BordersLineType.EdgeLeft).setColor(Color.BLACK);
        cell.getBorders().getByBordersLineType(BordersLineType.EdgeRight).setLineStyle(LineStyleType.Thin);
        cell.getBorders().getByBordersLineType(BordersLineType.EdgeRight).setColor(Color.BLACK);

//        CellRange cell = sheet.getRange().get("A11");
        cell.setValue("合并单元格");
        //将合并单元格的水平对齐方式设置为居中
        cell.getCellStyle().setHorizontalAlignment(HorizontalAlignType.Center);
        //将合并单元格的垂直对齐方式设置为居中
        cell.getCellStyle().setVerticalAlignment(VerticalAlignType.Center);

//        //设置行高、列宽为自适应（应用于指定数据范围）
//        sheet.getAllocatedRange().get("A1:E14").autoFitRows();
//        sheet.getAllocatedRange().get("A1:E14").autoFitColumns();

        //设置行高、列宽为自适应（应用于整个工作表）
        sheet.getAllocatedRange().autoFitRows();
        sheet.getAllocatedRange().autoFitColumns();

        //冻结前两行两列
        sheet.freezePanes(3,3);

        //设置水平分辨率及垂直分辨率
        workbook.getConverterSetting().setXDpi(300);
        workbook.getConverterSetting().setYDpi(300);
        //保存图片
        sheet.saveToImage("data/image.png");
        //保存结果文件
        workbook.saveToFile("data/CreateExcel.xlsx", FileFormat.Version2013);

    }
}
