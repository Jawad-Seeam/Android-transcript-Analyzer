workers = 2           # 2 sync workers is ideal for a free Render instance
threads = 1
bind = "0.0.0.0:10000"   # Render exposes port 10000
worker_class = "sync"
timeout = 120         # long enough for PDF analysis
accesslog = "-"       # log to stdout (visible in Render dashboard)
errorlog = "-"
loglevel = "info"
