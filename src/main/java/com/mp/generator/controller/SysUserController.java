package com.mp.generator.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mp.generator.entity.SysUser;
import com.mp.generator.service.ISysUserService;
import com.mp.utils.ExcelProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author ayetony
 * @since 2020-03-31
 */
@RestController
@RequestMapping("/generator/sysUser")
public class SysUserController {

    Logger logger = LoggerFactory.getLogger(ExcelProcess.class);

    @RequestMapping("/mp")
    public String map(){
        return "你好";
    }

    @Autowired
    ISysUserService iSysUserService;

    @RequestMapping("/hello")
    public List<SysUser> hello(){
        List<SysUser> list = iSysUserService.getBaseMapper().selectList(new QueryWrapper<SysUser>().lambda().like(SysUser::getUserInfo,"test"));
        return list;
    }

    @RequestMapping("/uploadExcel")
    public ResponseEntity<FileSystemResource> upload(@RequestParam("fileName") MultipartFile file, HttpServletResponse response) throws IOException {

        if(file.isEmpty()){
            response.getWriter().println("The file doest exist .");
            return null;
        }
        String fileName = file.getOriginalFilename();
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        File dest = new File("D:\\excel\\" + fileName);
        if(!dest.getParentFile().exists()){
            dest.mkdirs();
        }
        file.transferTo(dest);
        String absolutePath = dest.getAbsolutePath();
        //清洗excel
//        ExcelProcess.clean(absolutePath);
        ExcelProcess.format(absolutePath);
        logger.info("Format the excel : " + fileName + ">>Time:"+ LocalDateTime.now());
        //乱码问题
        response.setContentType("multipart/form-data;charset=UTF-8");
        File downloadFile = new File(absolutePath);
        return export(downloadFile);
    }


    public ResponseEntity<FileSystemResource> export(File file) {
        if (file == null) {
            return null;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", "attachment; filename=" + LocalDateTime.now() + ".xlsx");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Last-Modified", new Date().toString());
        headers.add("ETag", String.valueOf(System.currentTimeMillis()));

        return ResponseEntity.ok().headers(headers).contentLength(file.length()).contentType(MediaType.parseMediaType("application/octet-stream")).body(new FileSystemResource(file));
    }


    }
