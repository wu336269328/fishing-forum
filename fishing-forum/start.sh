#!/bin/bash
# 钓友圈 - 一键启动脚本
# 用法: bash start.sh

SUDO_PASS="wu336269328"
DIR="$(cd "$(dirname "$0")" && pwd)"

echo "=== 1. 启动 PostgreSQL ==="
echo "$SUDO_PASS" | sudo -S service postgresql start 2>/dev/null
sleep 1

# 检查数据库是否存在，不存在则创建
DB_EXISTS=$(echo "$SUDO_PASS" | sudo -S -u postgres psql -tAc "SELECT 1 FROM pg_database WHERE datname='fishing_forum'" 2>/dev/null)
if [ "$DB_EXISTS" != "1" ]; then
    echo "=== 创建数据库 fishing_forum ==="
    echo "$SUDO_PASS" | sudo -S -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'postgres';" 2>/dev/null
    echo "$SUDO_PASS" | sudo -S -u postgres psql -c "CREATE DATABASE fishing_forum;" 2>/dev/null
    echo "$SUDO_PASS" | sudo -S -u postgres psql -d fishing_forum < "$DIR/backend/src/main/resources/schema.sql" 2>/dev/null
    echo "$SUDO_PASS" | sudo -S -u postgres psql -d fishing_forum < "$DIR/backend/src/main/resources/data.sql" 2>/dev/null
    echo "数据库初始化完成"
fi
echo "PostgreSQL 已就绪"

echo "=== 2. 启动后端 (端口 8080) ==="
cd "$DIR/backend"
nohup mvn spring-boot:run > /tmp/fishforum-backend.log 2>&1 &
BACKEND_PID=$!
echo "后端 PID: $BACKEND_PID"

# 等待后端启动
echo -n "等待后端启动"
for i in $(seq 1 60); do
    if curl -s http://localhost:8080/api/sections > /dev/null 2>&1; then
        echo " 就绪!"
        break
    fi
    echo -n "."
    sleep 2
done

echo "=== 3. 启动前端 (端口 3000) ==="
cd "$DIR/frontend"
nohup npm run dev > /tmp/fishforum-frontend.log 2>&1 &
FRONTEND_PID=$!
echo "前端 PID: $FRONTEND_PID"
sleep 2

echo ""
echo "========================================="
echo "  钓友圈已启动!"
echo "  前端: http://localhost:3000"
echo "  后端: http://localhost:8080"
echo "  账号: admin / admin123"
echo "========================================="
echo ""
echo "查看日志:"
echo "  后端: tail -f /tmp/fishforum-backend.log"
echo "  前端: tail -f /tmp/fishforum-frontend.log"
echo ""
echo "停止服务: bash stop.sh"
