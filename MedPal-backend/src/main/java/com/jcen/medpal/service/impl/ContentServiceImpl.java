package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.medpal.mapper.ContentMapper;
import com.jcen.medpal.model.entity.Content;
import com.jcen.medpal.service.ContentService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ContentServiceImpl extends ServiceImpl<ContentMapper, Content> implements ContentService {
    
    @Override
    public Content createContent(Content content) {
        if (content.getStatus() == null || content.getStatus().trim().isEmpty()) {
            content.setStatus("draft");
        }
        content.setViewCount(content.getViewCount() == null ? 0 : content.getViewCount());
        content.setCreateTime(LocalDateTime.now());
        content.setUpdateTime(LocalDateTime.now());
        content.setIsDelete(0);
        this.save(content);
        return content;
    }
    
    @Override
    public boolean publishContent(Long id) {
        Content content = this.getById(id);
        if (content == null) {
            return false;
        }
        content.setStatus("published");
        content.setPublishTime(LocalDateTime.now());
        content.setUpdateTime(LocalDateTime.now());
        return this.updateById(content);
    }
    
    @Override
    public boolean unpublishContent(Long id) {
        Content content = this.getById(id);
        if (content == null) {
            return false;
        }
        content.setStatus("unpublished");
        content.setUpdateTime(LocalDateTime.now());
        return this.updateById(content);
    }
}
