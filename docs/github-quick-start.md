# GitHub 首次发布快速说明

这份文档是给你这个项目第一次上 GitHub 用的。

你现在本地项目已经有 Git 仓库了，但还没有连到远端 GitHub 仓库。

## 1. 先在 GitHub 网页上建空仓库

去 GitHub 新建一个仓库，建议名字直接用：

```text
digital-treehole
```

建议：

- 设成私有仓库也可以
- 不要勾选自动创建 README
- 不要勾选 `.gitignore`
- 不要勾选 license

原因很简单：本地项目已经有这些东西了，避免第一次 push 冲突。

## 2. 本地第一次提交

在 PowerShell 里进入项目目录：

```powershell
cd path\to\digital-treehole
```

查看状态：

```powershell
git status
```

把文件加入暂存区：

```powershell
git add .
```

做第一次提交：

```powershell
git commit -m "feat: 初始化数字树洞后端第一版"
```

## 3. 绑定远端仓库

把下面的地址换成你自己的 GitHub 仓库地址：

```powershell
git remote add origin https://github.com/你的用户名/digital-treehole.git
```

检查有没有绑成功：

```powershell
git remote -v
```

## 4. 推到 GitHub

```powershell
git push -u origin main
```

成功后，GitHub 网页里就能看到整个项目了。

## 5. 以后最常用的 4 个命令

```powershell
git status
git add .
git commit -m "说明这次改了什么"
git push
```

## 6. 如果你们 3 个人要协作

推完初版后，把前端同学和 AI 同学加到仓库协作者里。

然后建议他们：

- 不要直接在 `main` 上乱改
- 每个人建自己的分支
- 改完发 PR

分支命名规则看：

```text
docs/team-collaboration.md
```

## 7. 最容易出错的点

### GitHub 新仓库勾选了自动 README

这样第一次 push 可能会冲突。

### 仓库地址填错

最常见的是仓库名拼错，或者用户名写错。

### 没有登录 GitHub

第一次 push 时 Git 可能会让你登录 GitHub 账号，这是正常的。
