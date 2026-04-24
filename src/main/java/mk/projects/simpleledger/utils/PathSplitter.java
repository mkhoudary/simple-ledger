/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mk.projects.simpleledger.utils;

/**
 *
 * @author Mohammad
 */
public class PathSplitter {

    public static Pair<String, String> splitPath(String path) {
        int firstDotIndex = path.indexOf(".");
        if (firstDotIndex == -1) {
            return new Pair<>(path, "");
        }

        String firstPart = path.substring(0, firstDotIndex);
        String remainingPart = path.substring(firstDotIndex + 1);

        return new Pair<>(firstPart, remainingPart);
    }

    public static class Pair<T1, T2> {

        private T1 first;
        private T2 second;

        public Pair(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }

        public T1 getFirst() {
            return first;
        }

        public T2 getSecond() {
            return second;
        }
    }
}
