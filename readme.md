

Настройки в nginx
upstream tru_ftp_backend {
	server localhost:8088;
}

location ^~/eaist-tru-ftp { 
	proxy_set_header Host $host; 
	proxy_set_header X-Real-IP $remote_addr; 
	proxy_pass http://tru_ftp_backend/eaist-tru-ftp; 
}
