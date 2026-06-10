# 前端镜像：多阶段构建
# 阶段 1：在 Node 容器内执行 vite build
# 阶段 2：用 nginx 提供静态文件并反代 /api、/uploads 到后端
FROM node:18-alpine AS build

WORKDIR /app

# 先拷依赖描述以充分利用 docker layer 缓存
COPY package.json package-lock.json ./
RUN npm config set registry https://registry.npmmirror.com \
 && npm ci

COPY . .
RUN npm run build

# ---------- 运行阶段 ----------
FROM nginx:1.27-alpine

RUN rm -rf /usr/share/nginx/html/* \
 && mkdir -p /var/log/nginx

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
