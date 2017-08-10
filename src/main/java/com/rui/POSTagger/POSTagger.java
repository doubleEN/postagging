package com.rui.POSTagger;

import com.rui.model.FirstOrderHMM;
import com.rui.model.HMM;
import com.rui.model.SecondOrderHMM;
import com.rui.parameters.AbstractParas;
import com.rui.parameters.BigramParas;
import com.rui.parameters.TrigramParas;
import com.rui.tagger.Tagger;
import com.rui.wordtag.WordTag;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Properties;

/**
 * 汉语词性标注工具类
 */
public class POSTagger {

    public static void main(String[] args) {

//        AbstractParas paras=new BigramParas("/home/mjx/桌面/PoS/corpus/199801_format.txt",44,50000);
//        HMM hmm=new FirstOrderHMM(paras);
//        hmm.writeHMM("/home/mjx/桌面/BiGram.bin");
//
//        AbstractParas paras2=new TrigramParas("/home/mjx/桌面/PoS/corpus/199801_format.txt",44,50000);
//        HMM hmm2=new SecondOrderHMM(paras);
//        hmm.writeHMM("/home/mjx/桌面/TriGram.bin");
//
//
//        String[] sentences={
//                "谢谢  ！  （  新华社  北京  １２  ３１日  电  ）",
//                "在  十五大  精神  指引  下  胜利  前进  —— 元旦  献辞",
//                "在  这  辞旧迎新  的  美好  时刻  ，  我  祝  大家 新年  快乐  ，  家庭  幸福  ！",
//                "北京  举行  新年  音乐会"
//        };
//
//        for (String sentence:sentences){
//            WordTag[]wts=POSTagger.tag2Gram(sentence);
//            System.out.println(Arrays.toString(wts));
//        }
        AbstractParas paras=new BigramParas("/home/mjx/桌面/PoS/corpus/199801_format.txt");
        HMM hmm=new FirstOrderHMM(paras);
        Tagger tagger=new Tagger(hmm);
        System.out.println(Arrays.toString(tagger.tag("去年 夏季 ， 云岩区 在 繁华 街区 为 下岗 职工 专门 辟 出 一 段 街道 作为 夜市 摊位 ， 不少 职工 不仅 解决 了 生活 问题 ， 还 走 上 了 自谋 发展 的 路子 。 “ 两节 ” 期间 ， 市里 又 在 省 展览馆 等 年货 市场 ， 为 困难 职工 增加 了 上千 个 摊位 。 ")));
        System.out.println(Arrays.toString(tagger.tag("位于 博爱县 的 焦作市 农业 高 科技 示范 园区 ， 以 占地 １５ 亩 的 法国 温棚 为 中心 ， 东 西 各 是 ２０ 座 钢架 砖墙 的 高效 温棚 和 钢架 拱棚 ， “ 工人 ” 们 正 冒 着 雨 雪 把 整 箱 的 鲜花 、 蔬菜 装车 外运 。 而 在 这个 县 孝敬乡 的 焦作市 现代化 高效 农业 示范 园区 ， ４８ 条 水泥路 使 １．６万 亩 的 园区 分类 布局 ， 有的 已经 投产 。 全部 建成 后 ， 每年 可 产 鲜菜 ６０００万 公斤 。 位于 武陟县 黄河 岸边 的 国家 农业 开发 园区 ， 占地 近 万 亩 ， 集 生态 农业 和 观光 农业 于 一体 ， 现 已 部分 投产 ， 待 全部 建成 后 ， 每年 可 提供 １２００万 公斤 优良 粮种 ， ３００万 公斤 农副产品 。 据 介绍 ， 已经 建成 和 正在 建设 的 这 类 大型 园区 焦作 共有 ８ 个 ， 辐射 带动 了 各类 中小 园区 ３０ 来 个 。 ")));
        System.out.println(Arrays.toString(tagger.tag("最近 ， 广西 又 把 各 地 、 市 和 近 半数 县 的 主要 负责人 请 到 恭城 学习 观摩 。 将 以 大办 沼气 为 纽带 、 推动 养殖业 和 种植业 全面 发展 的 三位一体 的 “ 恭城 模式 ” 推广 到 全区 所有 的 县 、 市 、 乡 、 村 。 自治区 党委 明确 强调 ： 实行 地 、 县 、 乡 一把手 负责制 ， 全民 动员 ， 把 生态 农业 扎扎实实 地 “ 闹 ” 起来 。 为 此 ， 自治区 已 在 各地 确立 了 １００ 个 “ 生态村 ” 、 ５０ 个 “ 生态乡 ” 和 ２０ 个 “ 生态县 ” 示范点 。 ")));
        System.out.println(Arrays.toString(tagger.tag("北京市 东城区 、 朝阳区 ２５０ 位 老人 组成 的 秧歌队 进行 了 精彩 的 表演 。 北京 协和 医院 、 同仁 医院 、 东直门 医院 和 中华 医学会 北京 分会 的 有关 专家 为 老人 进行 了 测量 血压 、 乳透 、 血糖 快速 测定 、 骨密度 检查 等 １０ 余 项 义诊 服务 。 老年 朋友 们 排 起 了 长长的 队 ， 等待 义诊 。 队列 中 ， 一 位 白发 老人 感慨 地 对 记者 说 ： “ 尊老爱幼 是 中华民族 的 传统 美德 ， 义诊 为 我们 这些 老年人 真 解决 了 实际 问题 。 ” 部分 专家 和 学者 接受 了 老年 朋友 关于 老年 权益 保护 、 卫生 保健 、 心理 健康 、 婚姻 家庭 等 方面 的 咨询 。 北京市 老年病 防治 中心 的 专家 还 就 老年病 防治 问题 进行 了 讲解 。 ")));
        System.out.println(Arrays.toString(tagger.tag("这 一 现象 是 旧 的 农田水利 建设 与 管理 制度 存在 的 深 层次 矛盾 和 问题 的 外在 反映 。 长期以来 ， 农田水利 事业 的 发展 一直 受到 诸多 因素 困扰 ， 弊端 越来越 明显 ： 一 、 水利工程 的 所有者 和 使用者 之间 权责 不 明确 ， 利益 不 直接 ， 管理 水平 低 ， 致使 水利工程 老化 、 退化 ， 毁坏 严重 ， 难以 充分 发挥 使用 效益 。 费县 ３０００ 多 处 小型 农田水利 建设 ， 全部 是 由 国家 、 集体 投资 ， 加上 投 劳 兴建 起来 的 ， 所有权 归 国家 、 集体 ， 农民 仅 是 使用者 。 在 这种 情况 下 ， 作为 水利工程 的 主要 受益者 农民 ， 对 水利工程 的 维护 管理 并 不 关心 ， 以致 变电器 被盗 、 电线 被 割 、 闸门 被 毁 、 渠道 被 拆 状况 屡屡 发生 ， ３０％ 的 设施 无法 使用 。 二 、 水资源 浪费 严重 。 几十 年 来 大家 习惯 于 用 “ 大锅水 ” ， 对 水资源 不 加 珍惜 ， 浇地 采用 大水 漫灌 的 方式 ， 每亩 地 浇 一 遍 水 高 达 ２００ 多 立方米 ， 有的 竟 达 ３００ 多 立方米 ； 加之 管理 跟不上 ， 跑 、 冒 、 滴 、 漏 现象 严重 ， 水 的 利用率 还 不 到 ３０％ 。 三 、 农民 没有 树立 起 水 是 商品 的 观念 ， 收取 水费 困难 ， 每年 有 ３０％ — ４０％ 的 应 收 水费 收 不 上来 ， 致使 工程 维护 缺乏 资金 来源 。 ")));
        System.out.println(Arrays.toString(tagger.tag("费县 作为 山区 农业 大 县 ， 特定 的 区位 条件 和 十年九旱 的 气候 特点 ， 决定 了 水利 在 农业 生产 中 的 命脉 作用 。 费县 十分 重视 水利 建设 ， 到 １９８０年 ， 小型 水利 设施 就 发展 到 ３０００ 多 处 ， 灌溉 面积 达到 了 ２１．１５万 亩 ， 占 全县 有效 灌溉 面积 的 近 ４６％ 。 从 １９８０年 到 １９９５年 ， 费县 又 投资 ３０００ 多 万 元 新建 各类 小型 水利工程 ９３２ 处 。 然而 到 １９９６年 ， 全县 各类 小型 水利 设施 的 灌溉 面积 仍 为 ２１．１５万 亩 ， 几乎 １ 亩 也 没有 增加 。 ")));
        System.out.println(Arrays.toString(tagger.tag("军人 最 讲究 知己知彼 。 地处 杭 、 嘉 、 湖 平原 的 湖州市 ， 素以 生产 优质 丝绸 而 闻名 海内外 ， 真丝 面料 货源 充足 ， 且 价格 便宜 。 盛 云龙 看中 了 这 一点 ， 果敢 地 做出 转产 生产 高档 真丝 服装 系列 ， 进军 国际 市场 的 战略 决策 。 他 ３ 次 南 下 广州 和 深圳 ， １９９１年 １０月 如愿以偿 ： 第一 笔 高 达 １２６万 元 的 出口 服装 定单 终于 被 他 拿 到 手 。 备齐 资金 和 面料 ， 一连 １０ 多 天 昼夜 奋战 ， 第一 笔 生意 破天荒 赢利 ３０万 元 。 ")));
        System.out.println(Arrays.toString(tagger.tag("从 怀 揣 ６５０ 元 复员费 的 退伍兵 ， 到 拥有 几亿 资产 的 “ 大 老板 ” ")));
        System.out.println(Arrays.toString(tagger.tag("由于 “ 贵远贱近 ” 之 风 使 然 ， 今人 易 首肯 古代 诗人 哲人 ， 对 现代 学者 则 多 持 一 种 更 审慎 的 态度 。 近代 以来 ， 西风东渐 。 文化 上 的 中 西 之 争 、 古 今 之 争 ， 使 现代 学者 相对 于 古代 学者 而言 ， 似乎 有 了 更 大 的 承负 。 自 新 时期 改革 开放 以来 ， 知识界 学术界 关于 终极 关怀 、 雅俗 文化 、 兴 国学 、 回到 传统 语言 等 问题 的 论战 不 绝 ， 而 一些 冷静 清醒 的 学者 却 在 进行 着 细致 的 学术 清理 和 转译 工作 ， 在 思想 和 学术 广度 上 都 有 新 的 开拓 。 而 这 批 学者 视野 开阔 ， 思路 敏捷 ， 文风 谨严 而 锐利 ， 切入 问题 的 角度 不拘 格套 ， 展示 了 他们 每个 人 与众不同 的 学术 气质 和 思想 魅力 。 而且 他们 还 兼具 觉世 与 传世 的 双重 学术 良知 ， 既 有 对 终极 问题 的 形而上 思考 ， 又 有 对 当下 现实 的 深切 关怀 。 从 他们 各自 的 学术 历程 和 心灵 轨迹 来 看 ， 是 为 建筑 新 的 精神 大厦 而 不懈努力 ， 同时 对 转型期 的 读者 大众 益处 良多 。 ")));
        System.out.println(Arrays.toString(tagger.tag("本报 蚌埠 １月 １日 电 记者 黄 振中 、 白 剑峰 报道 ： 新年 的 钟声 刚刚 敲响 ， 千 里 淮河 传来 喜讯 ： 沿 淮 工业 污染源 实现 达标 排放 ， 削减 污染 负荷 ４０％ 以上 ， 淮河 治 污 第一 战役 告捷 。")));
        System.out.println(Arrays.toString(tagger.tag("本报 苏州 １月 １日 电 记者 龚 雯 报道 ： １９９７年 １２月 ３１日 子夜 ， 随着 苏州 寒山寺 钟声 撞 响 第１０８ 下 ， 姑苏 城内 一 片 欢腾 ， 中共中央 政治局 委员 、 国务委员 罗 干 在 此 郑重 宣布 ： ’ ９７ 中国 旅游年 圆满 结束 ， ’ ９８ 华夏 城乡游 正式 开幕 。 ")));

    }

    //2-gram标注
    public static WordTag[] tag2Gram(String sentence) {

        Properties pro = new Properties();
        Tagger tagger = null;
        InputStreamReader propertiesPath = null;
        try {
            propertiesPath = new InputStreamReader(new FileInputStream("target/classes/tag.properties"), "UTF-8");
            pro.load(propertiesPath);
            String BiGram = (String) pro.get("BiGram");
            tagger = new Tagger(BiGram);

            propertiesPath.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                propertiesPath.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tagger.tag(sentence);
    }

    //3-gram标注
    public static WordTag[] tag3Gram(String sentence) {
        Properties pro = new Properties();
        Tagger tagger = null;
        InputStreamReader propertiesPath = null;
        try {
            propertiesPath = new InputStreamReader(new FileInputStream("target/classes/tag.properties"), "UTF-8");
            pro.load(propertiesPath);
            String TriGram = (String) pro.get("TriGram");
            tagger = new Tagger(TriGram);

            propertiesPath.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                propertiesPath.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tagger.tag(sentence);
    }
}
