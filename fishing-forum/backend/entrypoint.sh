#!/bin/sh
# 复制鱼类图片到上传目录（如果存在）
if [ -d "/app/fish-images" ] && [ "$(ls -A /app/fish-images/*.jpg 2>/dev/null)" ]; then
  mkdir -p /app/uploads/images/fish
  cp -n /app/fish-images/*.jpg /app/uploads/images/fish/ 2>/dev/null || true
  echo "鱼类图片已复制到 uploads/images/fish/"
fi

exec java -jar app.jar "$@"
