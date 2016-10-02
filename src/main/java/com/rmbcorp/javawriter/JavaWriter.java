package com.rmbcorp.javawriter;

import com.rmbcorp.javawriter.clazz.Clazz;
import com.rmbcorp.javawriter.clazz.ClazzImplManager;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**Need to produce classes on the fly somehow...
 * Created by rmbdev on 9/5/2016.
 */
class JavaWriter {

    public static void main(String[] args) {
        try {
            buildFile();
        } catch (IOException e) {
        }
    }

    private static Clazz getClazz(ClazzImplManager clazzManager, String filename) {
        Clazz clazz = clazzManager.get("com.rmbcorp.javawriter.clazz", filename);
        clazz.setClassType(Clazz.ClassType.CLASS);
        clazz.addImports(Arrays.<Class>asList(Integer.class, String.class, String.class));
        clazz.setVisibility(Clazz.Visibility.PUBLIC);
        clazz.setFinal(true);
        clazz.addImplementations(Collections.<Class>singletonList(Clazz.class));
        return clazz;
    }

    private static void buildFile() throws IOException {
        Map<String, String> stuff = System.getenv();
        for (Map.Entry item : stuff.entrySet()) {
            System.out.println(item.getKey() + " : " + item.getValue());
        }

        String filename;
        System.out.println("name (omit .java please)? ");
        BufferedReader bin = new BufferedReader(new InputStreamReader(System.in));
        filename = bin.readLine();
        System.out.println(filename);
        ClazzImplManager clazzManager = ClazzImplManager.getInstance();
        Clazz clazz = getClazz(clazzManager, filename);

        String file = clazzManager.writeOut(clazz);
        mkdirs();
        String outputpath = "src/main/java/com/rmbcorp/javawriter/clazz";
        File f = new File(outputpath + "/" + filename + ".java");
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(file.getBytes());
        fos.close();

        Runtime r = Runtime.getRuntime();
        String classpath = "\"" + System.getenv().get("USERPROFILE") + "\\IdeaProjects\\JavaWriter\\target\\classes" + "\"";
        System.out.println(classpath);
        Process p = r.exec("javac " + "-classpath " + classpath + " -d bin " + outputpath + "/" + filename + ".java");
        try {
            BufferedReader bis = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            BufferedReader ber = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;
            while ( (line = ber.readLine()) != null) {
                System.out.println(line);
                bw.write(line);
            }
            System.out.println(p.waitFor() == 0 ? "javac success" : "javac failure");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void mkdirs() {
        boolean genCreated, binCreated;
        File f = new File("src/gen/");
        genCreated = f.exists() || f.mkdir();
        File d = new File("bin/");
        binCreated = d.exists() || d.mkdir();
        System.out.println("gen folder? " + genCreated + ", bin folder? " + binCreated);
    }

}