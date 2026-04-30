package com.fishforum.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public final class PageUtils {
    public static final int MAX_SIZE = 100;

    private PageUtils() {
    }

    public static int page(int page) {
        return Math.max(page, 1);
    }

    public static int size(int size) {
        if (size < 1) {
            return 1;
        }
        return Math.min(size, MAX_SIZE);
    }

    public static <T> Page<T> pageRequest(int page, int size) {
        return new Page<>(page(page), size(size));
    }
}
