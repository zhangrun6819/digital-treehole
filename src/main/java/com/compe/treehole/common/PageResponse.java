package com.compe.treehole.common;

import java.util.List;

public record PageResponse<T>(
        List<T> records,
        long total,
        long pageNo,
        long pageSize
) {
}
