
package com.google.cloud.healthcare.etl.xmltojson;

import java.io.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.*;


public class XmlToJsonMain {
    public static void main(String[] args) throws XmlToJsonException, IOException, TransformerException {
        XmlToJsonCDARev2 xmlToJsonCDARev2 = new XmlToJsonCDARev2();

        String inputFile = args[0];
        File xmlFile;

        if (args.length > 1 && args[1].equals("windows-1252")) {
            File windows1252XmlFile = new File(args[0]);
            String windows1252XmlFileName = windows1252XmlFile.getName();
            String utf8XmlFileName = windows1252XmlFile.getParent() + "\\" + windows1252XmlFileName.substring(0, windows1252XmlFileName.lastIndexOf(".")) + "_utf_8" + windows1252XmlFileName.substring(windows1252XmlFileName.lastIndexOf("."));
            utf8XmlFileName = utf8XmlFileName.replaceAll("\\\\","/");
            Source in = new StreamSource(windows1252XmlFile);
            TransformerFactory f = TransformerFactory.newInstance();
            Result out = new StreamResult(new File(utf8XmlFileName));
            f.newTransformer().transform(in, out);

            xmlFile = new File(utf8XmlFileName);
        }
        else {
            xmlFile = new File(inputFile);
        }

        // Let's get XML file as String using BufferedReader
        // FileReader uses platform's default character encoding
        // if you need to specify a different encoding,
        // use InputStreamReader
        Reader fileReader = new FileReader(xmlFile);
        BufferedReader bufReader = new BufferedReader(fileReader);
        StringBuilder sb = new StringBuilder();
        String line = bufReader.readLine();
        while (line != null) {
            sb.append(line).append("\n");
            line = bufReader.readLine();
        }
        String xml2String = sb.toString();

        bufReader.close();

        String jsonString = xmlToJsonCDARev2.parse(xml2String);

        System.out.println(jsonString);
    }
}