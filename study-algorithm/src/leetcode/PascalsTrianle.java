package leetcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 给定一个非负索引 k，其中 k ≤ 33，返回杨辉三角的第 k 行。
 *
 *
 *
 * 在杨辉三角中，每个数是它左上方和右上方的数的和。
 *
 * 示例:
 *
 * 输入: 3
 * 输出: [1,3,3,1]
 * 进阶：
 *
 * 你可以优化你的算法到 O(k) 空间复杂度吗？
 * @author zhanglaijie
 * @since 2019-08-19
 */

public class PascalsTrianle {

    public static List<Integer> getRow2(int rowIndex){
        List<Integer> result = Arrays.asList(1);
        for(int i=1;i<=rowIndex;i++){
            List<Integer> befor = new ArrayList<>();
            for(int j=0;j<=i;j++){
                if(j==0||j==i){
                    befor.add(1);
                }else {
                    befor.add(result.get(j-1)+result.get(j));
                }
            }
            result =befor;
        }
        return result;
    }


    public static List<Integer> getRow(int rowIndex) {
        List<Integer> result = new ArrayList<>();
        long num = 1;
        for(int i=0; i<=rowIndex; i++){
            result.add((int)num);
            num = num*(rowIndex-i)/(i+1);
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(getRow(33));
    }
}
