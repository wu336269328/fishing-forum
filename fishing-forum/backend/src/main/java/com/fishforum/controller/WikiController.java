package com.fishforum.controller;

import com.fishforum.common.Result;
import com.fishforum.entity.WikiEntry;
import com.fishforum.service.WikiService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 知识百科控制器
 */
@RestController
@RequestMapping("/api/wiki")
@RequiredArgsConstructor
public class WikiController {

    private final WikiService wikiService;

    // 获取词条列表（公开）
    @GetMapping
    public Result<?> listEntries(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return wikiService.listEntries(category, keyword, page, size);
    }

    // 获取词条详情（公开）
    @GetMapping("/{id}")
    public Result<?> getEntry(@PathVariable Long id) {
        return wikiService.getEntry(id);
    }

    // 获取分类列表（公开）
    @GetMapping("/categories")
    public Result<?> getCategories() {
        return wikiService.getCategories();
    }

    // 创建词条
    @PostMapping
    public Result<?> createEntry(@RequestBody WikiEntry entry, Authentication auth) {
        return wikiService.createEntry(entry, (Long) auth.getPrincipal());
    }

    // 编辑词条
    @PutMapping("/{id}")
    public Result<?> updateEntry(@PathVariable Long id, @RequestBody WikiEntry entry, Authentication auth) {
        return wikiService.updateEntry(id, entry, (Long) auth.getPrincipal());
    }

    // 删除词条
    @DeleteMapping("/{id}")
    public Result<?> deleteEntry(@PathVariable Long id, Authentication auth) {
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return wikiService.deleteEntry(id, (Long) auth.getPrincipal(), role);
    }

    // 获取版本历史（公开）
    @GetMapping("/{id}/history")
    public Result<?> getHistory(@PathVariable Long id) {
        return wikiService.getHistory(id);
    }
}
