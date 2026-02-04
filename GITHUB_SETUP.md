# GitHub Repository Setup

This document provides instructions for creating and pushing the BTC Watch Face project to GitHub.

## Prerequisites

1. GitHub account (https://github.com)
2. GitHub CLI (`gh`) or Git with HTTPS credentials
3. Repository visibility preference (public/private)

## Option 1: Using GitHub CLI (Recommended)

### Step 1: Authenticate with GitHub

```bash
gh auth login

# You'll be prompted:
# ? What account do you want to log in to? [GitHub.com]
# ? What is your preferred protocol for Git operations? [HTTPS]
# ? Authenticate Git with your GitHub credentials? [Y/n]
# ? How would you like to authenticate GitHub CLI? [Paste an authentication token]

# Or use:
gh auth login -h github.com --web
# (Opens browser for authentication)
```

### Step 2: Create Repository on GitHub

```bash
cd /home/admin/.openclaw/workspace/btcwatch-wff

# Create public repository
gh repo create btcwatch-wff --public --source=. --remote=origin --push

# Or create private repository
gh repo create btcwatch-wff --private --source=. --remote=origin --push
```

### Step 3: Verify Push

```bash
git remote -v
# Should show:
# origin  https://github.com/bronsonberry/btcwatch-wff.git (fetch)
# origin  https://github.com/bronsonberry/btcwatch-wff.git (push)

# Check GitHub
# Visit: https://github.com/bronsonberry/btcwatch-wff
```

## Option 2: Manual Setup via Web

### Step 1: Create Repository on GitHub.com

1. Go to https://github.com/new
2. Fill in repository details:
   - **Repository name**: `btcwatch-wff`
   - **Description**: "Production-ready Bitcoin-themed Wear OS watch face with WFF API"
   - **Visibility**: Public (recommended)
   - **Initialize this repository**: Do NOT check (we have local repo)
3. Click "Create repository"

### Step 2: Add Remote and Push

```bash
cd /home/admin/.openclaw/workspace/btcwatch-wff

# Add remote
git remote add origin https://github.com/bronsonberry/btcwatch-wff.git

# Rename branch to main (if desired)
git branch -M main

# Push
git push -u origin main
```

### Step 3: Input Credentials

When prompted, use one of:
- **GitHub Personal Access Token** (recommended)
  - Create at: https://github.com/settings/tokens
  - Scopes needed: `repo` (all), `workflow`
  - Use token as password
  
- **GitHub Password** (less secure, deprecated)
  - Use GitHub password directly

## Option 3: Using SSH (Advanced)

### Step 1: Generate SSH Key

```bash
ssh-keygen -t ed25519 -C "bronson@example.com"
# Or for RSA:
ssh-keygen -t rsa -b 4096 -C "bronson@example.com"

# Press Enter to use default location: ~/.ssh/id_ed25519
# Enter passphrase (optional)
```

### Step 2: Add Key to GitHub

```bash
# Copy public key
cat ~/.ssh/id_ed25519.pub

# Go to https://github.com/settings/keys
# Click "New SSH key"
# Paste public key content
# Title: "Development Machine"
# Add key
```

### Step 3: Push via SSH

```bash
cd /home/admin/.openclaw/workspace/btcwatch-wff

git remote add origin git@github.com:bronsonberry/btcwatch-wff.git
git branch -M main
git push -u origin main
```

## Complete Workflow

If everything is set up, simply run:

```bash
cd /home/admin/.openclaw/workspace/btcwatch-wff

# Verify git configuration
git config --get user.name
git config --get user.email

# Create repo and push (using GitHub CLI)
gh repo create btcwatch-wff \
  --public \
  --source=. \
  --remote=origin \
  --push \
  --description "Production-ready Bitcoin-themed Wear OS watch face with WFF API"

# Done! Check the repo
echo "Repository: https://github.com/bronsonberry/btcwatch-wff"
```

## Repository Settings

### After Repository Creation

1. **Protect Main Branch**
   - Settings → Branches → Add branch protection rule
   - Branch name: `main`
   - Require pull request reviews: Yes
   - Require status checks: Yes (add CI later)

2. **Add Topics**
   - Settings → Topics
   - Add: `wear-os`, `watchface`, `wff`, `bitcoin`, `android`

3. **Enable Issues**
   - Settings → Features
   - Check: Issues, Discussions

4. **Add README Badge** (optional)
   - Edit README.md
   - Add badges for build status, license, etc.

## Troubleshooting

### "Repository already exists"

```bash
# The remote already exists
git remote remove origin
git remote add origin https://github.com/bronsonberry/btcwatch-wff.git
git push -u origin main
```

### "Authentication failed"

```bash
# Clear git credentials cache
git credential-cache exit

# Try again with token
git push -u origin main
# When prompted for password, paste Personal Access Token
```

### "Permission denied (publickey)"

SSH key not set up. Use HTTPS instead:

```bash
git remote remove origin
git remote add origin https://github.com/bronsonberry/btcwatch-wff.git
git push -u origin main
```

## Creating Releases

After pushing:

```bash
# Create a release
gh release create v1.0.0 \
  --title "BTC Watch Face v1.0.0" \
  --notes "Production-ready release of Bitcoin-themed Wear OS watch face"

# Or via GitHub web UI:
# Releases → Draft a new release → v1.0.0 → Publish
```

## Next Steps

1. ✅ Repository created
2. ✅ Code pushed
3. ⬜ (Optional) Set up CI/CD with GitHub Actions
4. ⬜ (Optional) Configure branch protection rules
5. ⬜ Build APK and create release
6. ⬜ Submit to Google Play Console

## References

- [GitHub CLI Documentation](https://cli.github.com/manual)
- [Git Documentation](https://git-scm.com/doc)
- [GitHub REST API](https://docs.github.com/en/rest)

---

**Status**: Ready for GitHub push  
**Repository Name**: `btcwatch-wff`  
**Owner**: @bronsonberry  
