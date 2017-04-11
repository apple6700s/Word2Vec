package test;

import java.io.File;
import java.io.IOException;

import tech.zhangchi.vec.Learn;
import tech.zhangchi.vec.Word2Vec;

public class Test {
    public static void main(String[] args) throws IOException {
//        Word2Vec w1 = new Word2Vec() ;
//        w1.loadGoogleModel("library/corpus.bin") ;
        
//        System.out.println(w1.distance("奥尼尔"));
//
//        System.out.println(w1.distance("毛泽东"));
//
//        System.out.println(w1.distance("邓小平"));
//
//
//        System.out.println(w1.distance("魔术队"));
//
//        System.out.println(w1.distance("魔术"));

//         Learn learn = new Learn();
//         learn.learnFile(new File("D:\\data\\yuliao.txt"));
//         learn.saveModel(new File("word2vec_model"));

        Word2Vec vec = new Word2Vec();
        vec.loadGoogleModel("D:\\data\\wiki_chinese_word2vec(Google).model");
        System.out.println(vec.wordSimilarity("秋田狗","秋田犬"));

        // System.out.println("中国" + "\t" +
        // Arrays.toString(vec.getWordVector("中国")));
        // ;
        // System.out.println("毛泽东" + "\t" +
        // Arrays.toString(vec.getWordVector("毛泽东")));
        // ;
        // System.out.println("足球" + "\t" +
        // Arrays.toString(vec.getWordVector("足球")));

        // Word2Vec vec2 = new Word2Vec();
        // vec2.loadGoogleModel("library/vectors.bin") ;
        //
        //
//        String str = "毛泽东";
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < 100; i++) {
//            System.out.println(vec.distance(str));
//        }
//        System.out.println(System.currentTimeMillis() - start);
//
//        System.out.println(System.currentTimeMillis() - start);
        // System.out.println(vec2.distance(str));
        //
        //
        // //男人 国王 女人
        // System.out.println(vec.analogy("邓小平", "毛泽东思想", "毛泽东"));
        // System.out.println(vec2.analogy("毛泽东", "毛泽东思想", "邓小平"));
        
    }
}
