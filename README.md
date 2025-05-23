# APadWallPaper

## 简介
一款专为安卓平板设计的壁纸应用，支持在设备横竖屏切换时自动切换两张不同壁纸。用户可自定义横屏和竖屏模式下的壁纸图片，配置信息通过DataStore持久化存储，图片加载使用Glide框架实现。

## 核心功能
- 🔄 自动横竖屏壁纸切换
- 🖼️ 支持本地图片选择（支持PNG/JPEG/WebP格式）

## 使用说明

### 1. 设为系统壁纸
1. 主界面点击“设为壁纸”
2. 在列表中找到“APadWallpaper”
3. 点击“设置壁纸”，此时横竖屏各有一个内置壁纸

### 2. 切换模式
- 当设备旋转时自动触发切换壁纸

### 3. 修改壁纸
主界面“选择横屏壁纸”按钮和“选择竖屏壁纸”按钮。

## 技术栈
| 技术                | 说明                          |
|---------------------|-------------------------------|
| **Glide**           | 高性能图片加载框架            |
| **DataStore**       | 异步键值存储解决方案          |

### 许可证
MIT License
