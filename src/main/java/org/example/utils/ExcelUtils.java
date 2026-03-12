package org.example.utils;

import com.spire.xls.BordersLineType;
import com.spire.xls.LineStyleType;
import com.spire.xls.collections.BordersCollection;

import java.awt.*;

public class ExcelUtils {

    public static void setBordersAround(BordersCollection bordersCollection, LineStyleType lineStyleType, Color color) {
        //分别设置上、下、左、右边框
        bordersCollection.getByBordersLineType(BordersLineType.EdgeTop).setLineStyle(lineStyleType);
        bordersCollection.getByBordersLineType(BordersLineType.EdgeTop).setColor(color);
        bordersCollection.getByBordersLineType(BordersLineType.EdgeBottom).setLineStyle(lineStyleType);
        bordersCollection.getByBordersLineType(BordersLineType.EdgeBottom).setColor(color);
        bordersCollection.getByBordersLineType(BordersLineType.EdgeLeft).setLineStyle(lineStyleType);
        bordersCollection.getByBordersLineType(BordersLineType.EdgeLeft).setColor(color);
        bordersCollection.getByBordersLineType(BordersLineType.EdgeRight).setLineStyle(lineStyleType);
        bordersCollection.getByBordersLineType(BordersLineType.EdgeRight).setColor(color);
    }
}
