package com.ginka.shortlink.shortlink.admin.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 创建时间填充
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
       strictInsertFill(metaObject, "createTime", ()->new Date(), Date.class);
       strictInsertFill(metaObject, "updateTime", ()->new Date(), Date.class);
       strictInsertFill(metaObject, "deleteFlag", ()->0, Integer.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime",()->new Date(), Date.class);
    }
}
