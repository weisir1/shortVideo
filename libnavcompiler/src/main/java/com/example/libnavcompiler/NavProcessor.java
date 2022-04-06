package com.example.libnavcompiler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.libnavannotation.ActivityDestination;
import com.example.libnavannotation.FragmentDestination;
import com.google.auto.service.AutoService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)   //支持得源码类型 java1.8
//注解类型
@SupportedAnnotationTypes({"com.example.libnavannotation.FragmentDestination", "com.example.libnavannotation.ActivityDestination"})
public class NavProcessor extends AbstractProcessor {

    private Messager messager;
    private Filer filer;
    private static  final  String OUTPUT_FILE_NAME = "destnation.json";


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnvironment.getMessager();   //获取日志
        filer = processingEnvironment.getFiler();  //生成文件
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        FileOutputStream fos = null;
        OutputStreamWriter writer = null;
        Set<? extends Element> fragmentElements = roundEnvironment.getElementsAnnotatedWith(FragmentDestination.class);
        Set<? extends Element> activityElements = roundEnvironment.getElementsAnnotatedWith(ActivityDestination.class);

        if (!fragmentElements.isEmpty() || !activityElements.isEmpty()) {
            HashMap<String, JSONObject> destMap = new HashMap<>();
            handleDestination(fragmentElements, FragmentDestination.class, destMap);   //带有注解的fragment/activity相关属性加入map
            handleDestination(activityElements, ActivityDestination.class, destMap);
            //app/src/main/assets
            try {
                FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", OUTPUT_FILE_NAME);
                //默认位置:intermediates->javac->debug->classes
                String resourcePath = resource.toUri().getPath();
                messager.printMessage(Diagnostic.Kind.NOTE,resourcePath);
                String appPath = resourcePath.substring(0, resourcePath.indexOf("app") + 4);
                String assetsPath = appPath +"src/main/assets/";
                File file = new File(assetsPath);
                if (!file.exists()){  //如果没有创建路径
                    file.mkdirs();
                }
                File outputFile = new File(file, OUTPUT_FILE_NAME);  //如果已存在文件, 删除重建
                if (outputFile.exists()){
                    outputFile.delete();
                }
                outputFile.createNewFile();
                String content = JSON.toJSONString(destMap);  //将map对象转为json格式的字符串并存储到assets目录下
                fos = new FileOutputStream(outputFile);
                writer = new OutputStreamWriter(fos);
                writer.write(content);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (writer!=null){
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fos!=null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }

    private void handleDestination(Set<? extends Element> elements, Class<? extends Annotation> annotationClaz, HashMap<String, JSONObject> destMap) {
        for (Element element : elements) {   //遍历注解元素,element标记在类上
            TypeElement typeElement = (TypeElement) element;  //可以直接转换后获取全类名
            String pageUrl = null;
            String ClazName = typeElement.getQualifiedName().toString();  //获取注解名称
            int id = Math.abs(ClazName.hashCode());
            boolean needLogin = false;
            boolean asStarter = false;
            boolean isFragment = false;
            Annotation annotation = typeElement.getAnnotation(annotationClaz);
            if (annotation instanceof FragmentDestination){
                FragmentDestination destFragment = (FragmentDestination) annotation;
               pageUrl = destFragment.pageUrl();
               asStarter = destFragment.asStarter();
               needLogin = destFragment.needLogin();
               isFragment =true;
            }else if (annotation instanceof ActivityDestination){
                ActivityDestination destFragment = (ActivityDestination) annotation;
                pageUrl = destFragment.pageUrl();
                asStarter = destFragment.asStarter();
                needLogin = destFragment.needLogin();
                isFragment = true;
            }
            if (destMap.containsKey(pageUrl)){  //如果map中包含重复pageUrl,提示并且不添加
                messager.printMessage(Diagnostic.Kind.ERROR,"不同的页面不允许使用相同的pageUrl:"+ClazName);
            }else {
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("id",id);
                jsonObject.put("needLogin",needLogin);
                jsonObject.put("pageUrl",pageUrl);
                jsonObject.put("asStarter",asStarter);
                jsonObject.put("ClazName",ClazName);
                jsonObject.put("isFragment",isFragment);
                destMap.put(pageUrl,jsonObject);
            }

        }
    }
}
