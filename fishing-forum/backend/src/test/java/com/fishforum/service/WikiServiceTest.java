package com.fishforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fishforum.common.Result;
import com.fishforum.entity.User;
import com.fishforum.entity.WikiEntry;
import com.fishforum.entity.WikiHistory;
import com.fishforum.mapper.UserMapper;
import com.fishforum.mapper.WikiEntryMapper;
import com.fishforum.mapper.WikiHistoryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WikiServiceTest {

    @Mock WikiEntryMapper entryMapper;
    @Mock WikiHistoryMapper historyMapper;
    @Mock UserMapper userMapper;
    @InjectMocks WikiService wikiService;

    @Test
    void listEntriesPaginatesAndEnrichesAuthor() {
        WikiEntry entry = entry(7L, 3L);
        Page<WikiEntry> page = new Page<>(1, 10);
        page.setRecords(List.of(entry));
        page.setTotal(1);
        when(entryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        User user = new User(); user.setId(3L); user.setUsername("editor");
        when(userMapper.selectById(3L)).thenReturn(user);

        Result<?> result = wikiService.listEntries("鱼种", "鲫鱼", 1, 10);

        WikiEntry returned = (WikiEntry) ((List<?>) ((Map<?, ?>) result.getData()).get("records")).get(0);
        assertThat(returned.getAuthorName()).isEqualTo("editor");
    }

    @Test
    void getEntryIncrementsViewCount() {
        WikiEntry entry = entry(7L, 3L);
        entry.setViewCount(9);
        when(entryMapper.selectById(7L)).thenReturn(entry);

        Result<?> result = wikiService.getEntry(7L);

        assertThat(((WikiEntry) result.getData()).getViewCount()).isEqualTo(10);
        verify(entryMapper).updateById(entry);
    }

    @Test
    void createEntrySetsFirstVersionAndHistory() {
        WikiEntry entry = new WikiEntry();
        entry.setTitle("鲫鱼");
        entry.setContent("content");

        Result<?> result = wikiService.createEntry(entry, 3L);

        assertThat(result.getMessage()).isEqualTo("词条创建成功");
        assertThat(entry.getUserId()).isEqualTo(3L);
        assertThat(entry.getVersion()).isEqualTo(1);
        assertThat(entry.getViewCount()).isZero();
        verify(entryMapper).insert(entry);
        verify(historyMapper).insert(any(WikiHistory.class));
    }

    @Test
    void updateEntryBumpsVersionAndRecordsEditorHistory() {
        WikiEntry entry = entry(7L, 3L);
        entry.setVersion(2);
        WikiEntry update = new WikiEntry();
        update.setTitle("新标题");
        update.setCategory("技巧");
        update.setContent("new content");
        when(entryMapper.selectById(7L)).thenReturn(entry);

        Result<?> result = wikiService.updateEntry(7L, update, 4L);

        assertThat(result.getData()).isEqualTo("词条更新成功");
        assertThat(entry.getVersion()).isEqualTo(3);
        assertThat(entry.getTitle()).isEqualTo("新标题");
        verify(entryMapper).updateById(entry);
        verify(historyMapper).insert(argThat(h -> h.getUserId().equals(4L) && h.getVersion().equals(3)));
    }

    @Test
    void deleteEntryRequiresOwnerUnlessAdmin() {
        WikiEntry entry = entry(7L, 3L);
        when(entryMapper.selectById(7L)).thenReturn(entry);

        assertThat(wikiService.deleteEntry(7L, 4L, "USER").getCode()).isEqualTo(403);
        assertThat(wikiService.deleteEntry(7L, 4L, "ADMIN").getCode()).isEqualTo(200);
        verify(entryMapper).deleteById(7L);
    }

    @Test
    void categoriesAreStable() {
        @SuppressWarnings("unchecked")
        List<String> categories = (List<String>) wikiService.getCategories().getData();
        assertThat(categories)
                .contains("鱼种", "饵料", "装备", "技巧", "常识", "鱼种图鉴");
    }

    private static WikiEntry entry(Long id, Long userId) {
        WikiEntry entry = new WikiEntry();
        entry.setId(id);
        entry.setUserId(userId);
        entry.setTitle("鲫鱼");
        entry.setContent("content");
        entry.setCategory("鱼种");
        entry.setVersion(1);
        entry.setViewCount(0);
        return entry;
    }
}
