package com.cocna.pdffilereader.ui.test;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileToPDF {

    static final int wdDoNotSaveChanges = 0;// 不保存待定的更改。
    static final int wdFormatPDF = 17;// word转PDF 格式
    static final int ppSaveAsPDF = 32;// ppt 转PDF 格式

    public void WordToPDF(String source, String target) {

        long start = System.currentTimeMillis();
        ActiveXComponent app = null;
        try {
            app = new ActiveXComponent("Word.Application");
            app.setProperty("Visible", false);

            Dispatch docs = app.getProperty("Documents").toDispatch();
            System.out.println("Thuytv---docs" + source);
            Dispatch doc = Dispatch.call(docs, "Open", source, false, true).toDispatch();

            System.out.println("Thuytv---pdf " + target);
            File tofile = new File(target);
            if (tofile.exists()) {
                tofile.delete();
            }
            Dispatch.call(doc, "SaveAs", target, wdFormatPDF);

            Dispatch.call(doc, "Close", false);
            long end = System.currentTimeMillis();
            System.out.println("Thuytv---Success：" + (end - start) + "ms.");
        } catch (Exception e) {
            System.out.println("========Error:Thuytv--Error：" + e.getMessage());
        } finally {
            if (app != null)
                app.invoke("Quit", wdDoNotSaveChanges);
        }
    }

    public void PptToPDF(String source, String target) {
        long start = System.currentTimeMillis();
        ActiveXComponent app = null;
        try {
            app = new ActiveXComponent("Powerpoint.Application");
            Dispatch presentations = app.getProperty("Presentations").toDispatch();
            System.out.println("打开文档" + source);
            Dispatch presentation = Dispatch.call(presentations, "Open", source, true, true, false).toDispatch();

            System.out.println("转换文档到PDF " + target);
            File tofile = new File(target);
            if (tofile.exists()) {
                tofile.delete();
            }
            Dispatch.call(presentation, "SaveAs", target, ppSaveAsPDF);

            Dispatch.call(presentation, "Close");
            long end = System.currentTimeMillis();
            System.out.println("转换完成..用时：" + (end - start) + "ms.");
        } catch (Exception e) {
            System.out.println("========Error:文档转换失败：" + e.getMessage());
        } finally {
            if (app != null)
                app.invoke("Quit");
        }
    }

    public void ExcelToPDF(String source, String target) {
        long start = System.currentTimeMillis();
        ActiveXComponent app = new ActiveXComponent("Excel.Application");
        try {
            app.setProperty("Visible", false);
            Dispatch workbooks = app.getProperty("Workbooks").toDispatch();
            System.out.println("打开文档" + source);
            Dispatch workbook = Dispatch.invoke(workbooks, "Open", Dispatch.Method,
                    new Object[]{source, new Variant(false), new Variant(false)}, new int[3]).toDispatch();
            Dispatch.invoke(workbook, "SaveAs", Dispatch.Method,
                    new Object[]{target, new Variant(57), new Variant(false), new Variant(57), new Variant(57),
                            new Variant(false), new Variant(true), new Variant(57), new Variant(true),
                            new Variant(true), new Variant(true)},
                    new int[1]);
            Variant f = new Variant(false);
            System.out.println("转换文档到PDF " + target);
            Dispatch.call(workbook, "Close", f);
            long end = System.currentTimeMillis();
            System.out.println("转换完成..用时：" + (end - start) + "ms.");
        } catch (Exception e) {
            System.out.println("========Error:文档转换失败：" + e.getMessage());
        } finally {
            if (app != null) {
                app.invoke("Quit", new Variant[]{});
            }
        }
    }
}