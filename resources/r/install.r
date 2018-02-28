#!/usr/bin/env Rscript

# http://stackoverflow.com/questions/17705133/package-error-when-running-r-code-on-command-line
cran_rstudio_repo="http://cran.rstudio.com/"

install.packages("vioplot", repos = cran_rstudio_repo)
install.packages("beanplot", repos = cran_rstudio_repo)
install.packages("ggplot2", repos = cran_rstudio_repo)
install.packages("reshape2", repos = cran_rstudio_repo)
install.packages("functional", repos = cran_rstudio_repo)
install.packages("plyr", repos = cran_rstudio_repo)
install.packages("extrafont", repos = cran_rstudio_repo)
install.packages("scales", repos = cran_rstudio_repo)
# install.packages("tikzDevice", repos = cran_rstudio_repo)