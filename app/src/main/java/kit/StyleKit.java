//package kit;
//
//import javax.swing.text.BadLocationException;
//import javax.swing.text.Document;
//import javax.swing.text.SimpleAttributeSet;
//import javax.swing.text.StyleConstants;
//
//import view.MainWindow;
//
//public class StyleKit {
//
//    Document document;
//    JTextPane textPane;
//    String textString;
//    SimpleAttributeSet attrSet;
//
//    public StyleKit(Document _document, String str) {
//        document = _document;
//        textString = str;
//        attrSet = new SimpleAttributeSet();
//    }
//
//    public StyleKit(JTextPane textPane, Document _document, String str) {
//        this.textPane = textPane;
//        document = _document;
//        textString = str;
//        attrSet = new SimpleAttributeSet();
//    }
//
//    /**
//     * 设置字体颜色
//     */
//    public StyleKit color(Color color) {
//        StyleConstants.setForeground(attrSet, color);
//        return this;
//    }
//
//    /**
//     * 字体大小
//     */
//    public StyleKit fontSize(int fontSize) {
//        StyleConstants.setFontSize(attrSet, fontSize);
//        return this;
//    }
//
//    /**
//     * 字体加粗
//     */
//    public StyleKit bold(boolean bold) {
//        StyleConstants.setBold(attrSet, bold);
//        return this;
//    }
//
//    // 追加文本
//    public void append() {
//        try {
//            document.insertString(document.getLength(), textString, attrSet);
//        } catch (BadLocationException e) {
//            funcs.trace("insert text error");
//        }
//        MainWindow.logStream.setCaretPosition(MainWindow.logStream.getStyledDocument().getLength());
//    }
//}
