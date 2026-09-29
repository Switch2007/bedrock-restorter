package com.restoration.db;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BedrockDatabase {
    private static DB db;
    private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();

    public static void open(File dbDir) throws IOException {
        LOCK.writeLock().lock();
        try {
            if (db != null) return;
            Options options = new Options().createIfMissing(false);
            db = Iq80DBFactory.factory.open(dbDir, options);
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    public static byte[] get(byte[] key) {
        LOCK.readLock().lock();
        try {
            return (db != null) ? db.get(key) : null;
        } finally {
            LOCK.readLock().unlock();
        }
    }

    public static byte[] getSubChunkKey(int cx, int cz, int dimId, byte subY) {
        ByteBuffer buf = (dimId == 0)
                ? ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN).putInt(cx).putInt(cz).put((byte) 0x2F).put(subY)
                : ByteBuffer.allocate(14).order(ByteOrder.LITTLE_ENDIAN).putInt(cx).putInt(cz).putInt(dimId).put((byte) 0x2F).put(subY);
        return buf.array();
    }

    public static byte[] getChunkTagKey(int cx, int cz, int dimId, byte tag) {
        ByteBuffer buf = (dimId == 0)
                ? ByteBuffer.allocate(9).order(ByteOrder.LITTLE_ENDIAN).putInt(cx).putInt(cz).put(tag)
                : ByteBuffer.allocate(13).order(ByteOrder.LITTLE_ENDIAN).putInt(cx).putInt(cz).putInt(dimId).put(tag);
        return buf.array();
    }

    public static void close() throws IOException {
        LOCK.writeLock().lock();
        try {
            if (db != null) {
                db.close();
                db = null;
            }
        } finally {
            LOCK.writeLock().unlock();
        }
    }
}
