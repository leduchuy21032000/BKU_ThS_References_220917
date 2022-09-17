    .data

aprompt:    .asciiz "Nhap a = "
bprompt:    .asciiz "Nhap b = "
cprompt:	.asciiz "a + b = "

    .text

main:

    la $a0, aprompt		# print the prompt for a
    li $v0, 4
    syscall

    li $v0, 5			# read input as integer
    syscall
    move $a1, $v0		# store a in $a1

    la $a0, bprompt		# print the prompt for b
    li $v0, 4
    syscall

    li $v0, 5			# read input as integer
    syscall
    move $a2, $v0		# store b in $a2
    
    addi $sp, $sp, -8	# make space on stack to store 2 values
	sw $a1, 0($sp)		# save $a1 on stack
	sw $a2, 4($sp)		# save $a2 on stack
	
    jal sum				# call function
    
    la $a0, cprompt		# print the prompt for result
    li $v0, 4
    syscall

	lw $a0, 0($sp)		# get result from function
	addi $sp, $sp, 4
	
	li $v0, 1			# print result
    syscall
    
    li $v0, 10
    syscall
    
sum:
	lw $t0, 0($sp)		# $t0 = a
	lw $t1, 4($sp)		# $t1 = b
	addi $sp, $sp, 8
   
	add $t2, $t0, $t1	# t2 = a + b

	addi $sp, $sp, -4	# make space on stack to store the result
	sw $t2, 0($sp)		# save $t2 on stack
	jr $ra				# return to caller