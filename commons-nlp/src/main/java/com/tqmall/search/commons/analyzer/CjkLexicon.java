package com.tqmall.search.commons.analyzer;

import com.tqmall.search.commons.ac.AcBinaryTrie;
import com.tqmall.search.commons.ac.AcTrieNodeFactory;
import com.tqmall.search.commons.exception.LoadLexiconException;
import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.match.Hit;
import com.tqmall.search.commons.match.MatchBinaryReverseTrie;
import com.tqmall.search.commons.nlp.NlpConst;
import com.tqmall.search.commons.nlp.NlpUtils;
import com.tqmall.search.commons.trie.RootNodeType;
import com.tqmall.search.commons.trie.TrieNodeFactory;
import com.tqmall.search.commons.utils.SearchStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by xing on 16/2/8.
 * 中文分词词库, 包括汉语词库以及停止词, 提供最大, 最小, 全匹配, 通过{@link AcBinaryTrie}实现
 * 词库文件中, 每个词可以指定词的{@link TokenType}, 通过{@link TokenType#fromString(String)}解析对应类型, 默认{@link TokenType#CN}
 * 要实现懒加载, 可通过{@link CjkLexiconFactory}实现
 *
 * @see TokenType
 * @see TokenType#fromString(String)
 * @see CjkLexiconFactory
 */
public class CjkLexicon {

    private static final Logger log = LoggerFactory.getLogger(CjkLexicon.class);

    /**
     * 中文数字字符
     */
    public static final Set<Character> CN_NUM;

    static {
        Set<Character> set = new HashSet<>();
        for (char c : "零○〇一二两三四五六七八九十壹贰叁肆伍陆柒捌玖拾百千万亿拾佰仟萬億兆卅廿".toCharArray()) {
            set.add(c);
        }
        CN_NUM = Collections.unmodifiableSet(set);
    }

    private final AcBinaryTrie<TokenType> acBinaryTrie;

    private final MatchBinaryReverseTrie<TokenType> matchReverseBinaryTrie;

    private final Set<String> quantifiers;

    /**
     * 读取词库文件, 如果存在异常则抛出{@link LoadLexiconException}
     *
     * @param rootNodeType 根节点类型
     * @param lexiconPaths 词库文件列表
     * @see LoadLexiconException
     * @see TrieNodeFactory
     * @see AcTrieNodeFactory
     */
    public CjkLexicon(RootNodeType rootNodeType, Path... lexiconPaths) {
        matchReverseBinaryTrie = new MatchBinaryReverseTrie<>(rootNodeType.<TokenType>defaultTrie());
        long startTime = System.currentTimeMillis();
        quantifiers = new HashSet<>();
        final AcBinaryTrie.Builder<TokenType> acBuilder = AcBinaryTrie.build();
        log.info("start loading cjk lexicon, file size: " + lexiconPaths.length);
        long lineCount = NlpUtils.loadLexicon(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String s) {
                int index = s.indexOf(' ');
                TokenType tokenType;
                if (index < 0) {
                    tokenType = TokenType.CN;
                } else {
                    String str = s.substring(index + 1).trim();
                    tokenType = TokenType.fromString(str);
                    s = s.substring(0, index);
                    if (tokenType == null) {
                        log.warn("load cjk lexicon word: " + s + " tokenType: " + str + " is invalid, instead of " + TokenType.CN);
                    } else if (tokenType == TokenType.QUANTIFIER) {
                        quantifiers.add(s);
                    }
                }
                acBuilder.put(s, tokenType);
                matchReverseBinaryTrie.put(s, tokenType);
                return true;
            }
        }, lexiconPaths);
        acBinaryTrie = acBuilder.create(rootNodeType.<TokenType>defaultAcTrie());
        log.info("load cjk lexicon finish, laod " + lineCount + " words, total cost: " + (System.currentTimeMillis() - startTime) + "ms");

        NlpUtils.loadClassPathLexicon(CjkLexicon.class, NlpConst.QUANTIFIER_FILE_NAME, new Function<String, Boolean>() {
            @Override
            public Boolean apply(String s) {
                quantifiers.add(s);
                return true;
            }
        });
    }

    /**
     * full匹配, 尽可能的返回所有能够匹配到的结果
     *
     * @param text 待分词文本
     * @param off  待处理文本的起始位置
     * @param len  待处理文本的长度
     * @return 匹配结果
     */
    public List<Hit<TokenType>> fullMatch(char[] text, int off, int len) {
        return acBinaryTrie.match(text, off, len);
    }

    /**
     * 最大匹配
     *
     * @param text 待分词文本
     * @param off  待处理文本的起始位置
     * @param len  待处理文本的长度
     * @return 匹配结果
     */
    public List<Hit<TokenType>> maxMatch(char[] text, int off, int len) {
        return matchReverseBinaryTrie.maxMatch(text, off, len);
    }

    /**
     * 最小匹配
     *
     * @param text 待分词文本
     * @param off  待处理文本的起始位置
     * @param len  待处理文本的长度
     * @return 匹配结果
     */
    public List<Hit<TokenType>> minMatch(char[] text, int off, int len) {
        return matchReverseBinaryTrie.minMatch(text, off, len);
    }

    /**
     * 添加量词
     *
     * @return 添加是否成功
     */
    public boolean addQuantifier(String quantifier) {
        quantifier = SearchStringUtils.filterString(quantifier);
        return quantifier != null && quantifiers.add(quantifier.toLowerCase());
    }

    /**
     * 删除量词
     *
     * @return 添加是否成功
     */
    public boolean removeQuantifier(String quantifier) {
        quantifier = SearchStringUtils.filterString(quantifier);
        return quantifier != null && quantifiers.remove(quantifier.toLowerCase());
    }

    /**
     * 判断给定的词是否为量词
     */
    public boolean isQuantifier(String word) {
        return quantifiers.contains(word);
    }

}
