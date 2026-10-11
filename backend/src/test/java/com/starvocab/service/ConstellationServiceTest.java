package com.starvocab.service;

import com.starvocab.common.PageResult;
import com.starvocab.dto.AddStarsRequest;
import com.starvocab.dto.SetCreateRequest;
import com.starvocab.dto.SetVO;
import com.starvocab.dto.StarItem;
import com.starvocab.entity.ConstellationMember;
import com.starvocab.entity.ConstellationSet;
import com.starvocab.entity.StarWord;
import com.starvocab.mapper.ConstellationMemberMapper;
import com.starvocab.mapper.ConstellationSetMapper;
import com.starvocab.mapper.StarWordMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 星集（词集）服务单元测试：创建 / 加词去重 / 权限 / 解散 */
@ExtendWith(MockitoExtension.class)
class ConstellationServiceTest {

    @Mock ConstellationSetMapper setMapper;
    @Mock ConstellationMemberMapper memberMapper;
    @Mock StarWordMapper starWordMapper;
    @InjectMocks ConstellationService constellationService;

    private ConstellationSet set(long id, long userId, int official) {
        ConstellationSet s = new ConstellationSet();
        s.setId(id);
        s.setUserId(userId);
        s.setName("星集" + id);
        s.setWordCount(0);
        s.setIsOfficial(official);
        return s;
    }

    private StarWord word(long id, String text) {
        StarWord w = new StarWord();
        w.setId(id);
        w.setWord(text);
        w.setPhoneticUs("/t/");
        w.setDefinition("[{\"pos\":\"n.\",\"meanings\":[\"释义\"]}]");
        w.setFrequency(100);
        return w;
    }

    @Test
    @DisplayName("创建星集：初始 0 词、非官方、归属当前用户")
    void create_set() {
        doAnswer(inv -> {
            inv.getArgument(0, ConstellationSet.class).setId(7L);
            return 1;
        }).when(setMapper).insert(any(ConstellationSet.class));

        SetVO vo = constellationService.create(1L, new SetCreateRequest("考研冲刺", "高频词"));
        assertThat(vo.id()).isEqualTo(7L);
        assertThat(vo.name()).isEqualTo("考研冲刺");
        assertThat(vo.wordCount()).isZero();
        assertThat(vo.isOfficial()).isFalse();
        assertThat(vo.isMine()).isTrue();
        assertThat(vo.joined()).isTrue();
    }

    @Test
    @DisplayName("加入星集：重复与不存在的词跳过，仅新增的计入 word_count")
    void addStars_dedup() {
        when(setMapper.selectById(3L)).thenReturn(set(3L, 1L, 0));
        when(starWordMapper.selectById(42L)).thenReturn(word(42L, "abundance"));
        when(starWordMapper.selectById(43L)).thenReturn(null); // 不存在
        when(starWordMapper.selectById(44L)).thenReturn(word(44L, "diligent"));
        // 42 不在集内（0），44 已在集内（1）
        when(memberMapper.selectCount(any())).thenReturn(0L, 1L);
        when(memberMapper.insert(any(ConstellationMember.class))).thenReturn(1);

        String message = constellationService.addStars(1L, 3L, new AddStarsRequest(List.of(42L, 43L, 44L)));
        assertThat(message).contains("1 颗星辰已纳入星集").contains("2 颗已在或不存在");

        ArgumentCaptor<ConstellationSet> captor = ArgumentCaptor.forClass(ConstellationSet.class);
        verify(setMapper).updateById(captor.capture());
        assertThat(captor.getValue().getWordCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("加入星集：非本人星集拒绝修改")
    void addStars_notOwner() {
        when(setMapper.selectById(3L)).thenReturn(set(3L, 2L, 0));
        assertThatThrownBy(() -> constellationService.addStars(1L, 3L, new AddStarsRequest(List.of(1L))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("无权修改");
    }

    @Test
    @DisplayName("浏览星集内单词：他人私有星集拒绝访问")
    void stars_permissionDenied() {
        ConstellationSet privateSet = set(3L, 2L, 0);
        when(setMapper.selectById(3L)).thenReturn(privateSet);
        assertThatThrownBy(() -> constellationService.stars(1L, 3L, 1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("无权访问");
    }

    @Test
    @DisplayName("浏览星集内单词：按成员顺序分页返回")
    void stars_paged() {
        when(setMapper.selectById(3L)).thenReturn(set(3L, 1L, 0));
        ConstellationMember m1 = new ConstellationMember();
        m1.setSetId(3L);
        m1.setWordId(42L);
        ConstellationMember m2 = new ConstellationMember();
        m2.setSetId(3L);
        m2.setWordId(43L);
        when(memberMapper.selectList(any())).thenReturn(List.of(m1, m2));
        when(starWordMapper.selectBatchIds(anyList())).thenReturn(List.of(word(42L, "abundance")));

        PageResult<StarItem> page = constellationService.stars(1L, 3L, 1, 1);
        assertThat(page.total()).isEqualTo(2);
        assertThat(page.list()).hasSize(1);
        assertThat(page.list().get(0).word()).isEqualTo("abundance");
        assertThat(page.totalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("解散星集：官方星集不可解散")
    void delete_officialBlocked() {
        when(setMapper.selectById(1L)).thenReturn(set(1L, 1L, 1));
        assertThatThrownBy(() -> constellationService.delete(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("官方星集不可解散");
    }

    @Test
    @DisplayName("解散星集：只能解散自己的星集，解散时清空成员")
    void delete_notOwner() {
        when(setMapper.selectById(1L)).thenReturn(set(1L, 2L, 0));
        assertThatThrownBy(() -> constellationService.delete(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("只能解散自己的星集");
    }

    @Test
    @DisplayName("解散星集：成功后成员与星集一并删除")
    void delete_success() {
        ConstellationSet s = set(1L, 1L, 0);
        s.setWordCount(2);
        when(setMapper.selectById(1L)).thenReturn(s);
        when(memberMapper.delete(any())).thenReturn(2);

        constellationService.delete(1L, 1L);
        verify(memberMapper).delete(any());
        verify(setMapper).deleteById(1L);
    }
}
