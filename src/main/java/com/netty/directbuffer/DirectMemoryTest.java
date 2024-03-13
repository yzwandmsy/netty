package com.netty.directbuffer;

import java.nio.ByteBuffer;

/**
 * 直接内存与堆内存的区别
 */
public class DirectMemoryTest {

    public static void heapAccess() {
        long startTime = System.currentTimeMillis();
        //分配堆内存
        ByteBuffer buffer = ByteBuffer.allocate(1000);
        for (int i = 0; i < 100000; i++) {
            for (int j = 0; j < 200; j++) {
                buffer.putInt(j);
            }

            /**
             * 该函数是Java NIO中的Buffer类的一个方法，
             * 它的作用是将Buffer的当前位置（position）重置为0，
             * 并将限制（limit）设置为之前的位置。
             * 在读取数据时，通常先调用flip()方法，
             * 这样可以将Buffer从写模式切换到读模式。
             */
            buffer.flip();
            for (int j = 0; j < 200; j++) {
                buffer.getInt();
            }
            buffer.clear();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("堆内存访问:" + (endTime - startTime));
    }

    public static void directAccess() {
        long startTime = System.currentTimeMillis();
        //分配直接内存
        ByteBuffer buffer = ByteBuffer.allocateDirect(1000);
        for (int i = 0; i < 100000; i++) {
            for (int j = 0; j < 200; j++) {
                buffer.putInt(j);
            }
            buffer.flip();
            for (int j = 0; j < 200; j++) {
                buffer.getInt();
            }
            buffer.clear();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("直接内存访问:" + (endTime - startTime));
    }

    public static void heapAllocate() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            ByteBuffer.allocate(100);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("堆内存申请:" + (endTime - startTime));
    }

    public static void directAllocate() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            ByteBuffer.allocateDirect(100);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("直接内存申请:" + (endTime - startTime));
    }

    public static void main(String args[]) {
        for (int i = 0; i < 10; i++) {
            heapAccess();
            directAccess();
        }

        System.out.println();

        for (int i = 0; i < 10; i++) {
            heapAllocate();
            directAllocate();
        }
    }
}