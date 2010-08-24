set syntax=scala
setl sw=2 sts=2 ts=2
source $HOME/.vim/scalacommenter.vim 
map <leader>sc :call ScalaCommentWriter()<CR> 
map <leader>fsc :call ScalaCommentFormatter()<CR> 
map cm :call ScalaCommentWriter()<CR> 
map cf :call ScalaCommentFormatter()<CR> 
let b:scommenter_class_svn_id = ''
let b:scommenter_class_author='Brendan W. McAdams <bmcadams@novus.com>' 
let b:scommenter_file_author='Brendan W. McAdams <bmcadams@novus.com>' 
let b:scommenter_company_name = 'Novus Partners, Inc. <http://novus.com>'
"let g:scommenter_file_copyright_list = [ 
"    \    'Copyright (c) 2009, 2010 Novus Partners, Inc. <http://novus.com>',
"	\'',
"	\' Licensed under the Apache License, Version 2.0 (the "License");',
"	\' you may not use this file except in compliance with the License.',
"	\' You may obtain a copy of the License at',
"	\'',
"	\'     http://www.apache.org/licenses/LICENSE-2.0',
"	\'',
"	\' Unless required by applicable law or agreed to in writing, software',
"	\' distributed under the License is distributed on an "AS IS" BASIS,',
"	\' WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.',
"	\' See the License for the specific language governing permissions and',
"	\' limitations under the License.',
"	\'',
"    \' For questions and comments about this product, please see the project page at:',
"    \'      http://github.com/novus/casbah',
"    \'',
"    \] 

function! SCommenter_OwnFileComments()
    call append(0,  '/**')
    call append(1, ' * Copyright (c) 2009, 2010 Novus Partners, Inc. <http://novus.com>')
	call append(2, ' * ')
	call append(3, ' * Licensed under the Apache License, Version 2.0 (the "License");')
	call append(4, ' * you may not use this file except in compliance with the License.')
	call append(5, ' * You may obtain a copy of the License at')
	call append(6, ' * ')
	call append(7, ' *   http://www.apache.org/licenses/LICENSE-2.0')
	call append(8, ' *')
	call append(9, ' * Unless required by applicable law or agreed to in writing, software')
	call append(10, ' * distributed under the License is distributed on an "AS IS" BASIS,')
	call append(11, ' * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.')
	call append(12, ' * See the License for the specific language governing permissions and')
	call append(13, ' * limitations under the License.')
	call append(14, ' *')
    call append(15, ' * For questions and comments about this product, please see the project page at:')
    call append(16, ' *')
    call append(17, ' *     http://github.com/novus/casbah')
    call append(18, ' * ')
    call append(19, ' */')
    call append(20, '')
endfunction

let tlist_scala_settings = 'scala;p:packages;c:classes;t:traits;T:types;m:methods;C:constants;l:local variables;c:case classes;o:objects;r:defs'

if has("autocmd") && exists("+omnifunc")
   autocmd Filetype *
\   if &omnifunc == "" |
\       setlocal omnifunc=syntaxcomplete#Complete |
\   endif
endif

command! UseSBT call SBTProject()
function! SBTProject()
    set makeprg=sbt-no-color\ compile
    set efm=%E\ %#[error]\ %f:%l:\ %m,%C\ %#[error]\ %p^,%-C%.%#,%Z,
            \%W\ %#[warn]\ %f:%l:\ %m,%C\ %#[warn]\ %p^,%-C%.%#,%Z,
            \%-G%.%#
endfunction


command! UseMVN call MavenProject()
function! MavenProject()
    compiler! maven2
endfunction

"autocmd BufNewFile,BufRead pom.xml compiler maven2

" toggles the quickfix window.
command! -bang -nargs=? QFix call QFixToggle(<bang>0)
function! QFixToggle(forced)
  if exists("g:qfix_win") && a:forced == 0
    cclose
  else
    execute "copen " . g:jah_Quickfix_Win_Height
  endif
endfunction

" used to track the quickfix window
augroup QFixToggle
 autocmd!
 autocmd BufWinEnter quickfix let g:qfix_win = bufnr("$")
 autocmd BufWinLeave * if exists("g:qfix_win") && expand("<abuf>") == g:qfix_win | unlet! g:qfix_win | endif
augroup END

nmap <silent> \` :QFix<CR>
nmap <silent> <F4> :QFix<CR>
nmap <silent> <F5> :make<CR>
nmap <silent> <S-F5> :make test<CR>
let g:jah_Quickfix_Win_Height=10

:UseSBT
