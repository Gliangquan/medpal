package com.jcen.medpal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcen.medpal.model.entity.Content;

public interface ContentService extends IService<Content> {
    
    Content createContent(Content content);
    
    boolean publishContent(Long id);
    
    boolean unpublishContent(Long id);
}
