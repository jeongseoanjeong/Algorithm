package com.example.Algorithm.controller;

import com.example.Algorithm.DTO.DTO;
import com.example.Algorithm.addDesign;
import com.example.Algorithm.qdb.qdb;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.util.UriEncoder;

import java.io.*;
import java.util.Objects;
import java.util.Random;

@Controller
@Slf4j
public class compilercontroller {
    public void write(String fileName, String cmd)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(cmd);

        writer.close();
    }
    private static String OS = System.getProperty("os.name").toLowerCase();
    private Model model,model2;
    private qdb questions;

    @RequestMapping("/result")
    public String res(@RequestParam(value = "res", required = false) String res, @RequestParam(value = "msg", required = false) String msg, Model m) {
        log.info("res:" + res);
        log.info("msg:" + msg);
        m.addAttribute("newLineChar", "\n");
        m.addAttribute("res", res);
        m.addAttribute("msg", msg);
        if (res == null && msg == null) {
            m.addAttribute("res", model.getAttribute("res"));
            m.addAttribute("msg", model.getAttribute("msg"));
            if (model.getAttribute("msg").toString().length() > 0) {
                m.addAttribute("msg", model.getAttribute("msg"));
                m.addAttribute("res", "~~No result~~");
            }

        }
        return "result";
    }
    public int countChar(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }
    @CrossOrigin(origins = "http://localhost:8080")
    @ResponseBody
    @RequestMapping("/format/**")
    public String format(HttpServletRequest request,Model m) throws IOException{
        addDesign.init(addDesign.MAGENTA,new String[]{"#include","#if","#define","#elif","#endif","#"});
        addDesign.init(addDesign.GOLD,new String[]{"if","else","elif","while","for","do","goto","return"});
        addDesign.init(addDesign.SKYBLUE,new String[]{"int","void","def","public","private","protected","double","float"});
        String code=request.getParameter("code").replaceAll(":;","\r\n");
        String lang=request.getParameter("lang");
        StringBuilder sb = new StringBuilder();
        int indent=0;

        String resuit="";
        if(lang.equals("c")||lang.equals("cpp")) {
            code = UriEncoder.decode(code);
            log.info("received:" + code);
            Process process;
            String[] cmd = {"/bin/sh", "-c", "echo \"" + code + "\" | clang-format"};
            process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            int cnt = 0;
            while ((line = reader.readLine()) != null) {
                cnt += 1;
                sb.append(line);
            }
            if (cnt == 0) {
                return "null";
            }
        }else if(lang.equals("python2_7")||lang.equals("python3")){
            code = UriEncoder.decode(code);
            log.info("received:" + code);
            Process process;
            String[] cmd = {"/bin/sh", "-c", "black -c \""+code+"\"" };
            process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            int cnt = 0;
            while ((line = reader.readLine()) != null) {
                cnt += 1;
                sb.append(line);
            }
            //log.error(String.valueOf(code.split("\r\n").length));

            if(code.split("\r\n").length<=1){
                if(code.contains(":")&&!code.endsWith(":")) {
                    String[] t=code.split(":");
                    return t[0]+":"+"\r\n\t"+t[1];
                }
                return sb.toString();
            }


            String[] temp=code.split("\r\n");
            sb=new StringBuilder();

            for(int i=0;i< temp.length;i++){
                if(temp[i].contains(":")&&countChar(temp[i+1],'\t')==indent){
                    indent+=1;
                    String temp2="";
                    for(int j=0;j<indent;j++) {
                       temp2+="\t";
                    }
                    sb.append(temp2).append(temp[i + 1]).append("\r\n");
                }else if(countChar(temp[i+1],'\t')==indent){
                    String temp2="";
                    for(int j=0;j<indent;j++) {
                        temp2+="\t";
                    }
                    sb.append(temp2).append(temp[i + 1]).append("\r\n");
                }else if(indent>0){
                    indent-=1;
                }
            }
            StringBuilder sb2=new StringBuilder();
            StringBuilder str = new StringBuilder();
            for(int i=0;i<indent;i++){
                str.append("\t");

            }
            code=sb.toString();
            /* 결함 발견: 수정 예정
            StringBuilder code2=new StringBuilder();
            for(String str2 :code.split("\r\n")){
                for(String tok:str2.split(" ")){
                    code2.append("<p class=\""+addDesign.convert(tok)+"\">"+tok+"</p>");
                }
            }

            code=code2.toString();
          */
            sb2.append(code);
            log.info("returned:"+ sb2);
            return UriEncoder.encode(sb2.append(str).toString());

        }
        code=sb.toString();
        StringBuilder code2=new StringBuilder();
        log.info(code);
        /* 결함 발견: 수정 예정
        for(String str2 :code.split("\r\n")){
            for(String tok:str2.split(" ")){
                code2.append("<p class=\""+addDesign.convert(tok)+"\">"+tok+"</p>");
                log.info(tok);
            }
        }
        code=code2.toString();

         */
        log.info("returned:"+ sb);
        return UriEncoder.encode(code);
    }
    @GetMapping("/question")
    public String question(Model m) {
        new File(System.getProperty("user.home") + "/q/").mkdirs();
        int i = new Random().nextInt(0, qdb.questions.length);
        m.addAttribute("question", qdb.questions[i]);
        m.addAttribute("in", qdb.input[i]);
        m.addAttribute("out", qdb.output[i]);
        if (i == 0) {
            m.addAttribute("num", "0");
        } else {
            m.addAttribute("num", String.valueOf(i));
        }
        return "question";
    }

    @PostMapping("/evaluate")
    public String evaluate(HttpServletRequest req, Model m) throws IOException, InterruptedException {
        String command, lang, num, userid;
        command = req.getParameter("command").replace(":;","\r\n");
        lang = req.getParameter("lang");
        num = String.valueOf(req.getParameter("num"));
        userid = req.getParameter("userid");
        log.info(command);
        if (num.equals("")) {
            num = "0";
        }
        if (userid == null || userid.equals("")) {
            userid = "guest" + Integer.toString(new Random().nextInt(0, 100000));
            try {
                new File("/tmp/" + userid).mkdirs();
            } catch (Exception ignored) {

            }

        }
        DTO dto = new DTO(command, lang);
        ProcessBuilder pb = new ProcessBuilder();
        ProcessBuilder pb2 = new ProcessBuilder("/tmp/a");
        Process process;
        if (Objects.equals(lang, "c?") || Objects.equals(lang, "c")) {
            write("/tmp/" + userid + "/a.c", command);
            String[] cmd = {"/bin/bash", "-c", "gcc -Wall /tmp/" + userid + "/a.c -o /tmp/" + userid + "/a"};
            process = Runtime.getRuntime().exec(cmd);
        } else if (Objects.equals(lang, "cpp?") || Objects.equals(lang, "cpp")) {
            write("/tmp/" + userid + "/a.cpp", command);
            String[] cmd = {"/bin/bash", "-c", "g++ -Wall /tmp/" + userid + "/a.cpp -o /tmp/" + userid + "/a"};
            process = Runtime.getRuntime().exec(cmd);

        } else if (Objects.equals(lang, "python2_7")) {
            write("/tmp/" + userid + "/a.py", command);
            String[] cmd = {"/bin/bash", "-c", "pylint -E /tmp/" + userid + "/a.py"};
            process = Runtime.getRuntime().exec(cmd);

        } else if (Objects.equals(lang, "python3")) {
            write("/tmp/" + userid + "/a.py", command);
            String[] cmd = {"/bin/bash", "-c", "pylint -E /tmp/" + userid + "/a.py"};
            process = Runtime.getRuntime().exec(cmd);

        } else {
            return null;
        }
        if (process != null) {
            process.waitFor();
        }
        try {
            // Run script
            String line="", line0="";
            StringBuilder output = new StringBuilder();
            StringBuilder output1 = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(Objects.requireNonNull(process).getErrorStream()));

            if(Objects.equals(lang, "python2_7")||Objects.equals(lang, "python3")){
                reader = new BufferedReader(
                        new InputStreamReader(Objects.requireNonNull(process).getInputStream()));
            }



            //File file = new File("/tmp/a");
            //while(!file.exists()){
            // log.warn("File not exist!");
            //}

            String[] cmd = {"/bin/bash", "-c", "/tmp/" + userid + "/a"};
            if (Objects.equals(lang, "python3")) {
                cmd= new String[]{"/bin/bash", "-c", "python3  /tmp/" + userid + "/a.py"};
            }else if(Objects.equals(lang, "python2_7")){
                cmd= new String[]{"/bin/bash", "-c", "python /tmp/" + userid + "/a.py"};
            }
            int cnt = 0, cnt0 = 0,cnt1=0;

            while ((line0 = reader.readLine()) != null) {
                cnt0 += 1;
                if((Objects.equals(lang, "python2_7")||Objects.equals(lang, "python3")&&cnt0>2)||Objects.equals(lang, "cpp")||Objects.equals(lang, "c")) {
                    output1.append(line0).append("\r\n");
                    log.info(line0);
                }
            }
            // Read output
            Process process1 = Runtime.getRuntime().exec(cmd);
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(process1.getInputStream()));
            BufferedReader reader3 = new BufferedReader(new FileReader(System.getProperty("user.home") + "/q/" + num));
            int lines = 0;
            if (cnt0 > 0) {
                m.addAttribute("msg", output1.toString());
                m.addAttribute("res", "~~err~~");
            } else {
                String line2 = "";
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process1.getOutputStream()));
                line2 = reader3.readLine();
                writer.write(line2.split("::")[0] + "\r\n");
                writer.flush();
                line = reader2.readLine();
                if(line!=null) {

                    if (line.trim().equals(line2.split("::")[1].trim())) {

                        log.info(line);
                        cnt += 1;
                    }
                    lines += 1;
                    while ((line2 = reader3.readLine()) != null) {
                        log.info("evaluating:" + cnt);
                        Process process3 = Runtime.getRuntime().exec(cmd);
                        writer = new BufferedWriter(new OutputStreamWriter(process3.getOutputStream()));
                        reader2 = new BufferedReader(new InputStreamReader(process3.getInputStream()));
                        log.info(line2);
                        writer.write(line2.split("::")[0] + "\r\n");
                        writer.flush();
                        line = reader2.readLine();
                        if (line.trim().equals(line2.split("::")[1].trim())) {

                            log.info(line);
                            cnt += 1;

                        }
                        lines += 1;

                    }
                }
            /*
            Cookie cookie = new Cookie("msg",output1.toString()), cookie1 = new Cookie("res",output.toString());
            cookie.setSecure(true);
            cookie1.setSecure(true);
            response.addCookie(cookie);
            response.addCookie(cookie1);
             */
                if (cnt == lines&&cnt>0) {
                    m.addAttribute("res", Integer.toString(cnt) + "/" + Integer.toString(lines) + "(만점!)");
                } else {
                    m.addAttribute("res", Integer.toString(cnt) + "/" + Integer.toString(lines));
                }
                m.addAttribute("msg", output1.toString());

            }

            model = m;
            return "redirect:/result";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/run")
    public String exec(HttpServletRequest req, Model m) throws IOException, InterruptedException {
        String command, lang;
        command = req.getParameter("command");
        lang = req.getParameter("lang");
        DTO dto = new DTO(command, lang);
        ProcessBuilder pb = new ProcessBuilder();
        ProcessBuilder pb2 = new ProcessBuilder("/tmp/a");
        Process process;
        if (Objects.equals(lang, "c?") || Objects.equals(lang, "c")) {
            write("/tmp/a.c", command);
            String[] cmd = {"/bin/bash", "-c", "gcc -Wall /tmp/a.c -o /tmp/a"};
            process = Runtime.getRuntime().exec(cmd);
        } else if (Objects.equals(lang, "cpp?") || Objects.equals(lang, "cpp")) {
            write("/tmp/a.cpp", command);
            String[] cmd = {"/bin/bash", "-c", "g++ -Wall /tmp/a.cpp -o /tmp/a"};
            process = Runtime.getRuntime().exec(cmd);

        } else {
            return null;
        }
        if (process != null) {
            process.waitFor();
        }
        try {
            // Run script
            String line, line0;
            StringBuilder output = new StringBuilder();
            StringBuilder output1 = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(Objects.requireNonNull(process).getErrorStream()));
           // File file = new File("/tmp/a");
            //while(!file.exists()){
            // log.warn("File not exist!");
            //}
            String[] cmd = {"/bin/bash", "-c", "/tmp/a"};
            int cnt = 0, cnt0 = 0;
            Process process1 = Runtime.getRuntime().exec(cmd);
            while ((line0 = reader.readLine()) != null) {
                cnt0 += 1;
                output1.append(line0 + "\r\n");
                log.info(line0);
            }
            // Read output
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process1.getOutputStream()));
            writer.write(Objects.requireNonNull(req.getParameter("stdin")).toString()+"\r\n");
            writer.flush();
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(process1.getInputStream()));


            while ((line = reader2.readLine()) != null) {
                output.append(line).append("\r\n");
                log.info(line);
                cnt += 1;
            }
            /*
            Cookie cookie = new Cookie("msg",output1.toString()), cookie1 = new Cookie("res",output.toString());
            cookie.setSecure(true);
            cookie1.setSecure(true);
            response.addCookie(cookie);
            response.addCookie(cookie1);
             */

            m.addAttribute("msg", output1.toString());
            m.addAttribute("res", output.toString());
            model = m;
            return "redirect:/result";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}