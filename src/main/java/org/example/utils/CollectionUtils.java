package org.example.utils;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class CollectionUtils {

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断 collectionA 是否完全包含 collectionB
     * @param collectionA
     * @param collectionB
     * @return
     * @param <T>
     */
    public static <T> boolean isAbsolutelyContain(Collection<T> collectionA, Collection<T> collectionB) {
        List<T> listA = new ArrayList<>(collectionA);
        List<T> listB = new ArrayList<>(collectionB);
        for (int i = 0; i < listB.size(); i++) {
            T t = listB.get(i);
            if (listA.contains(t)) {
                listA.remove(t);
                listB.remove(i);
                i--;
            }
        }
        if (listB.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }


    public static boolean isEqual(Collection<String> list1, Collection<String> list2) {
        if (list1 == list2) {
            return true;
        } else if (CollectionUtils.isEmpty(list1) && CollectionUtils.isEmpty(list2)) {
            return true;
        } else if (CollectionUtils.isEmpty(list1) || CollectionUtils.isEmpty(list2)) {
            return false;
        } else {
            return isAbsolutelyContain(list1, list2) && isAbsolutelyContain(list2, list1);
        }
    }

    /**
     * 给出所有可能的排列组合（不考虑顺序）
     * @return
     * @param <T>
     */
    public static <T, E> List<List<E>> getAllCombinationList(List<T> listT, Function<T, E> func) {
        List<List<E>> allListList = new LinkedList<>();
        allListList.add(new ArrayList<>()); // 初始化
        for (T t: listT) {
            List<List<E>> eListList = new LinkedList<>();
            for (List<E> eList: allListList) {
                // 插入一种不放入slot的选项
                eListList.add(new LinkedList<>(eList));

                List<E> newEList = new LinkedList<>(eList);
                E e = func.apply(t);
                newEList.add(e);
                eListList.add(newEList);
            }
            allListList = eListList;
        }
        return allListList;
    }

    public static void main(String[] args) {
        List<Integer> listT = Lists.newArrayList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16);
        System.out.println(getAllCombinationList(listT, x -> x));
    }
}
