package org.keycloak.dashboard.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class FreeMarker {

    private File targetDir;
    private Map<String, Object> attributes;
    private Configuration configuration;

    public FreeMarker(File targetDir, Map<String, Object> attributes) {
        this.targetDir = targetDir;
        this.attributes = attributes;

        configuration = new Configuration(Configuration.VERSION_2_3_31);
        configuration.setClassLoaderForTemplateLoading(FreeMarker.class.getClassLoader(), "/");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
    }

    public void template(String template) throws IOException, TemplateException {
        Template t = configuration.getTemplate(template);
        File out = targetDir.toPath().resolve(template.replaceAll(".ftl", ".html")).toFile();

        File parent = out.getParentFile();
        if (!parent.isDirectory()) {
            parent.mkdir();
        }

        Writer w = new FileWriter(out);
        t.process(attributes, w);
    }

}