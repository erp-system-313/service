package com.erp.helpdesk.service;

import com.erp.common.dto.PageResponse;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.helpdesk.dto.CreateKbArticleRequest;
import com.erp.helpdesk.dto.KbArticleDto;
import com.erp.helpdesk.entity.KbArticle;
import com.erp.helpdesk.repository.KbArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KbService {

    private final KbArticleRepository kbArticleRepository;

    public PageResponse<KbArticleDto> findAll(int page, int size, boolean publishedOnly) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());

        Page<KbArticle> articles;
        if (publishedOnly) {
            articles = kbArticleRepository.findByIsPublishedTrue(pageable);
        } else {
            articles = kbArticleRepository.findAll(pageable);
        }

        return PageResponse.from(articles.map(KbArticleDto::fromEntity));
    }

    public PageResponse<KbArticleDto> search(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<KbArticle> articles = kbArticleRepository.searchPublished(query, pageable);
        return PageResponse.from(articles.map(KbArticleDto::fromEntity));
    }

    public KbArticleDto findById(Long id) {
        KbArticle article = kbArticleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KbArticle", id));
        return KbArticleDto.fromEntity(article);
    }

    @Transactional
    public KbArticleDto create(CreateKbArticleRequest request, Long userId) {
        KbArticle article = KbArticle.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .categoryId(request.getCategoryId())
                .tags(request.getTags())
                .isPublished(request.getIsPublished() != null ? request.getIsPublished() : false)
                .createdBy(userId)
                .build();

        article = kbArticleRepository.save(article);
        log.info("Created KB article: {}", article.getTitle());
        return KbArticleDto.fromEntity(article);
    }

    @Transactional
    public KbArticleDto update(Long id, CreateKbArticleRequest request) {
        KbArticle article = kbArticleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KbArticle", id));

        if (request.getTitle() != null) article.setTitle(request.getTitle());
        if (request.getContent() != null) article.setContent(request.getContent());
        if (request.getCategoryId() != null) article.setCategoryId(request.getCategoryId());
        if (request.getTags() != null) article.setTags(request.getTags());
        if (request.getIsPublished() != null) article.setIsPublished(request.getIsPublished());

        article = kbArticleRepository.save(article);
        log.info("Updated KB article: {}", article.getTitle());
        return KbArticleDto.fromEntity(article);
    }

    @Transactional
    public void delete(Long id) {
        kbArticleRepository.deleteById(id);
        log.info("Deleted KB article with id: {}", id);
    }

    @Transactional
    public void incrementViews(Long id) {
        kbArticleRepository.findById(id).ifPresent(article -> {
            article.setViews(article.getViews() + 1);
            kbArticleRepository.save(article);
        });
    }
}
