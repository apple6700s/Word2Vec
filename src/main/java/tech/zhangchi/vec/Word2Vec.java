package tech.zhangchi.vec;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import tech.zhangchi.vec.domain.WordEntry;

public class Word2Vec {

	private HashMap<String, float[]> wordMap = new HashMap<String, float[]>();

	private int words;
	private int size;
	private int topNSize = 40;

	/**
	 * 加载模型
	 * 
	 * @param path
	 *            模型的路径
	 * @throws IOException
	 */
	public void loadGoogleModel(String path) throws IOException {
		DataInputStream dis = null;
		BufferedInputStream bis = null;
		double len = 0;
		float vector = 0;
		try {
			bis = new BufferedInputStream(new FileInputStream(path));
			dis = new DataInputStream(bis);
			// //读取词数
			words = Integer.parseInt(readString(dis));
			// //大小
			size = Integer.parseInt(readString(dis));
			String word;
			float[] vectors = null;
			for (int i = 0; i < words; i++) {
				word = readString(dis);
				vectors = new float[size];
				len = 0;
				for (int j = 0; j < size; j++) {
					vector = readFloat(dis);
					len += vector * vector;
					vectors[j] = (float) vector;
				}
				len = Math.sqrt(len);

				for (int j = 0; j < size; j++) {
					vectors[j] /= len;
				}

				wordMap.put(word, vectors);
				dis.read();
			}
		} finally {
			if (bis != null) {
				bis.close();
			}
			if (dis != null) {
				dis.close();
			}
		}
	}

	/**
	 * 加载模型
	 * 
	 * @param path
	 *            模型的路径
	 * @throws IOException
	 */
	public void loadJavaModel(String path) throws IOException {
		try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(path)))) {
			words = dis.readInt();
			size = dis.readInt();

			float vector = 0;

			String key = null;
			float[] value = null;
			for (int i = 0; i < words; i++) {
				double len = 0;
				key = dis.readUTF();
				value = new float[size];
				for (int j = 0; j < size; j++) {
					vector = dis.readFloat();
					len += vector * vector;
					value[j] = vector;
				}

				len = Math.sqrt(len);

				for (int j = 0; j < size; j++) {
					value[j] /= len;
				}
				wordMap.put(key, value);
			}

		}
	}

	private static final int MAX_SIZE = 50;

	/**
	 * 近义词
	 * 
	 * @return
	 */
	public TreeSet<WordEntry> analogy(String word0, String word1, String word2) {
		float[] wv0 = getWordVector(word0);
		float[] wv1 = getWordVector(word1);
		float[] wv2 = getWordVector(word2);

		if (wv1 == null || wv2 == null || wv0 == null) {
			return null;
		}
		float[] wordVector = new float[size];
		for (int i = 0; i < size; i++) {
			wordVector[i] = wv1[i] - wv0[i] + wv2[i];
		}
		float[] tempVector;
		String name;
		List<WordEntry> wordEntrys = new ArrayList<WordEntry>(topNSize);
		for (Entry<String, float[]> entry : wordMap.entrySet()) {
			name = entry.getKey();
			if (name.equals(word0) || name.equals(word1) || name.equals(word2)) {
				continue;
			}
			float dist = 0;
			tempVector = entry.getValue();
			for (int i = 0; i < wordVector.length; i++) {
				dist += wordVector[i] * tempVector[i];
			}
			insertTopN(name, dist, wordEntrys);
		}
		return new TreeSet<WordEntry>(wordEntrys);
	}

	private void insertTopN(String name, float score, List<WordEntry> wordsEntrys) {
		// TODO Auto-generated method stub
		if (wordsEntrys.size() < topNSize) {
			wordsEntrys.add(new WordEntry(name, score));
			return;
		}
		float min = Float.MAX_VALUE;
		int minOffe = 0;
		for (int i = 0; i < topNSize; i++) {
			WordEntry wordEntry = wordsEntrys.get(i);
			if (min > wordEntry.score) {
				min = wordEntry.score;
				minOffe = i;
			}
		}

		if (score > min) {
			wordsEntrys.set(minOffe, new WordEntry(name, score));
		}

	}

	public Set<WordEntry> distance(String queryWord) {

		float[] center = wordMap.get(queryWord);
		if (center == null) {
			return Collections.emptySet();
		}

		int resultSize = wordMap.size() < topNSize ? wordMap.size() : topNSize;
		TreeSet<WordEntry> result = new TreeSet<WordEntry>();

		double min = Float.MIN_VALUE;
		for (Map.Entry<String, float[]> entry : wordMap.entrySet()) {
			float[] vector = entry.getValue();
			float dist = 0;
			for (int i = 0; i < vector.length; i++) {
				dist += center[i] * vector[i];
			}

			if (dist > min) {
				result.add(new WordEntry(entry.getKey(), dist));
				if (resultSize < result.size()) {
					result.pollLast();
				}
				min = result.last().score;
			}
		}
		result.pollFirst();

		return result;
	}

	public Set<WordEntry> distance(List<String> words) {

		float[] center = null;
		for (String word : words) {
			center = sum(center, wordMap.get(word));
		}

		if (center == null) {
			return Collections.emptySet();
		}

		int resultSize = wordMap.size() < topNSize ? wordMap.size() : topNSize;
		TreeSet<WordEntry> result = new TreeSet<WordEntry>();

		double min = Float.MIN_VALUE;
		for (Map.Entry<String, float[]> entry : wordMap.entrySet()) {
			float[] vector = entry.getValue();
			float dist = 0;
			for (int i = 0; i < vector.length; i++) {
				dist += center[i] * vector[i];
			}

			if (dist > min) {
				result.add(new WordEntry(entry.getKey(), dist));
				if (resultSize < result.size()) {
					result.pollLast();
				}
				min = result.last().score;
			}
		}
		result.pollFirst();

		return result;
	}

	private float[] sum(float[] center, float[] fs) {
		// TODO Auto-generated method stub

		if (center == null && fs == null) {
			return null;
		}

		if (fs == null) {
			return center;
		}

		if (center == null) {
			return fs;
		}

		for (int i = 0; i < fs.length; i++) {
			center[i] += fs[i];
		}

		return center;
	}

	/**
	 * 得到词向量
	 * 
	 * @param word
	 * @return
	 */
	public float[] getWordVector(String word) {
		return wordMap.get(word);
	}

	public static float readFloat(InputStream is) throws IOException {
		byte[] bytes = new byte[4];
		is.read(bytes);
		return getFloat(bytes);
	}

	/**
	 * 读取一个float
	 * 
	 * @param b
	 * @return
	 */
	public static float getFloat(byte[] b) {
		int accum = 0;
		accum = accum | (b[0] & 0xff);
		accum = accum | (b[1] & 0xff) << 8;
		accum = accum | (b[2] & 0xff) << 16;
		accum = accum | (b[3] & 0xff) << 24;
		return Float.intBitsToFloat(accum);
	}

	/**
	 * 读取一个字符串
	 * 
	 * @param dis
	 * @return
	 * @throws IOException
	 */
	private static String readString(DataInputStream dis) throws IOException {
		// TODO Auto-generated method stub
		byte[] bytes = new byte[MAX_SIZE];
		byte b = dis.readByte();
		int i = -1;
		StringBuilder sb = new StringBuilder();
		while (b != 32 && b != 10) {
			i++;
			bytes[i] = b;
			b = dis.readByte();
			if (i == 49) {
				sb.append(new String(bytes));
				i = -1;
				bytes = new byte[MAX_SIZE];
			}
		}
		sb.append(new String(bytes, 0, i + 1));
		return sb.toString();
	}

	public int getTopNSize() {
		return topNSize;
	}

	public void setTopNSize(int topNSize) {
		this.topNSize = topNSize;
	}

	public HashMap<String, float[]> getWordMap() {
		return wordMap;
	}

	public int getWords() {
		return words;
	}

	public int getSize() {
		return size;
	}

	/**
	 * 计算向量内积
	 * @param vec1
	 * @param vec2
	 * @return
	 */
	private float calDist(float[] vec1, float[] vec2) {
		float dist = 0;
		for (int i = 0; i < vec1.length; i++) {
			dist += vec1[i] * vec2[i];
		}
		return dist;
	}
	/**
	 * 计算词相似度
	 * @param word1
	 * @param word2
	 * @return
	 */
	public float wordSimilarity(String word1, String word2) {
		float[] word1Vec = getWordVector(word1);
		float[] word2Vec = getWordVector(word2);
		if(word1Vec == null || word2Vec == null) {
			return 0;
		}
		return calDist(word1Vec, word2Vec);
	}
	/**
	 * 获取相似词语
	 * @param word
	 * @param maxReturnNum
	 * @return
	 */
	public Set<WordEntry> getSimilarWords(String word, int maxReturnNum) {
		float[] center = getWordVector(word);
		if (center == null) {
			return Collections.emptySet();
		}
		int resultSize = getWords() < maxReturnNum ? getWords() : maxReturnNum;
		TreeSet<WordEntry> result = new TreeSet<WordEntry>();
		double min = Double.MIN_VALUE;
		for (Map.Entry<String, float[]> entry : getWordMap().entrySet()) {
			float[] vector = entry.getValue();
			float dist = calDist(center, vector);
			if (result.size() <= resultSize) {
				result.add(new WordEntry(entry.getKey(), dist));
				min = result.last().score;
			} else {
				if (dist > min) {
					result.add(new WordEntry(entry.getKey(), dist));
					result.pollLast();
					min = result.last().score;
				}
			}
		}
		result.pollFirst();
		return result;
	}
	/**
	 * 计算词语与词语列表中所有词语的最大相似度
	 * (最小返回0)
	 * @param centerWord 词语
	 * @param wordList 词语列表
	 * @return
	 */
	private float calMaxSimilarity(String centerWord, List<String> wordList) {
		float max = -1;
		if (wordList.contains(centerWord)) {
			return 1;
		} else {
			for (String word : wordList) {
				float temp = wordSimilarity(centerWord, word);
				if (temp == 0) continue;
				if (temp > max) {
					max = temp;
				}
			}
		}
		if (max == -1) return 0;
		return max;
	}
	/**
	 * 计算句子相似度
	 * 所有词语权值设为1
	 * @param sentence1Words 句子1词语列表
	 * @param sentence2Words 句子2词语列表
	 * @return 两个句子的相似度
	 */
	public float sentenceSimilarity(List<String> sentence1Words, List<String> sentence2Words) {
		if (sentence1Words.isEmpty() || sentence2Words.isEmpty()) {
			return 0;
		}
		float[] vector1 = new float[sentence1Words.size()];
		float[] vector2 = new float[sentence2Words.size()];
		for (int i = 0; i < vector1.length; i++) {
			vector1[i] = calMaxSimilarity(sentence1Words.get(i), sentence2Words);
		}
		for (int i = 0; i < vector2.length; i++) {
			vector2[i] = calMaxSimilarity(sentence2Words.get(i), sentence1Words);
		}
		float sum1 = 0;
		for (int i = 0; i < vector1.length; i++) {
			sum1 += vector1[i];
		}
		float sum2 = 0;
		for (int i = 0; i < vector2.length; i++) {
			sum2 += vector2[i];
		}
		return (sum1 + sum2) / (sentence1Words.size() + sentence2Words.size());
	}
	/**
	 * 计算句子相似度(带权值)
	 * 每一个词语都有一个对应的权值
	 * @param sentence1Words 句子1词语列表
	 * @param sentence2Words 句子2词语列表
	 * @param weightVector1 句子1权值向量
	 * @param weightVector2 句子2权值向量
	 * @return 两个句子的相似度
	 * @throws Exception 词语列表和权值向量长度不同
	 */
	public float sentenceSimilarity(List<String> sentence1Words, List<String> sentence2Words, float[] weightVector1, float[] weightVector2) throws Exception {
		if (sentence1Words.isEmpty() || sentence2Words.isEmpty()) {
			return 0;
		}
		if (sentence1Words.size() != weightVector1.length || sentence2Words.size() != weightVector2.length) {
			throw new Exception("length of word list and weight vector is different");
		}
		float[] vector1 = new float[sentence1Words.size()];
		float[] vector2 = new float[sentence2Words.size()];
		for (int i = 0; i < vector1.length; i++) {
			vector1[i] = calMaxSimilarity(sentence1Words.get(i), sentence2Words);
		}
		for (int i = 0; i < vector2.length; i++) {
			vector2[i] = calMaxSimilarity(sentence2Words.get(i), sentence1Words);
		}
		float sum1 = 0;
		for (int i = 0; i < vector1.length; i++) {
			sum1 += vector1[i] * weightVector1[i];
		}
		float sum2 = 0;
		for (int i = 0; i < vector2.length; i++) {
			sum2 += vector2[i] * weightVector2[i];
		}
		float divide1 = 0;
		for (int i = 0; i < weightVector1.length; i++) {
			divide1 += weightVector1[i];
		}
		float divide2 = 0;
		for (int j = 0; j < weightVector2.length; j++) {
			divide2 += weightVector2[j];
		}
		return (sum1 + sum2) / (divide1 + divide2);
	}


}
