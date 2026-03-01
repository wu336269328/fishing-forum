#!/bin/bash
# 钓友圈 - 一键停止脚本
echo "停止前端..."
pkill -f "vite" 2>/dev/null
echo "停止后端..."
pkill -f "fishforum" 2>/dev/null
echo "已停止"
