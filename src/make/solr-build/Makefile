NAME:=your-dataset-name-here

vpath %.py /remote/bones/gnat/toolkit/src/python/

IDLIKEIFY:=gsub("  *","-",
IDIFY=$(IDLIKEIFY)id);
PYTHONIFY:=gsub(",[ ]*","\",\"",

all:
	@echo First,
	@echo make package
	@echo
	@echo Then, to install,
	@echo From curtis, do
	@echo /opt/solr/bin/post -c $(NAME) post/\*.json

package: seed.json entity.json ontology-categories.json ontology-relations.json label-categories.json label-relations.json predictions-categories.json predictions-relations.json
	rm -rf post
	mkdir post
	mv $^ post/
	mkdir -p secondary
	mv *.json secondary


## the below scripts are based on the software/stack overflow
## dataset. Modify based on your site's output.



# join results:
# tagid tagname dbpid score
seed.json: task3_join/annotatedMatches.gp buildJson.py
	awk 'BEGIN{FS=OFS="\t"}{print "seed-" $$1, $$2, $$3, $$4}' $< | tr -d '\r' | \
	python $(word 2,$^) id,innerarg1,innerarg2,innerscore doctype=seed source=ss/guineapig innerarg1src=stackOverflow innerarg2src=dbpedia entitytype=grounding > $@

# all relevant tags:
# tagid tagname excerpt
entity.json: corpus/excerpts.tsv buildJson.py
	awk 'BEGIN{FS=OFS="\t"}{print "tag-" $$1, $$2, $$3}' $< | tr -d '\r' | \
	python $(word 2,$^) id,innername,innertext doctype=entity source=stackOverflow entitytype=category > $@

# categories:
# name desc parent mutex dbpedia
ontology-categories.json: task5.5_ontology/categories.tsv buildJson.py
	grep -v "^#" $< | \
	awk 'BEGIN{FS=OFS="\t"}{ \
	id="category-" $$1; ${IDIFY} \
	gsub(",",", category ",$$4); ${PYTHONIFY}$$4); ${IDLIKEIFY}$$4); ${PYTHONIFY}$$5); \
	if (length($$4)>0) { $$4 = "\"category-" $$4 "\""; } \
	if (length($$5)>0) { $$5 = "\"" $$5 "\""; } \
	print id,$$1,$$3,"@python[" $$5 "]","@python[" $$4 "]",$$2}' | tail -n +2 | tr -d '\r' | \
	python $(word 2,$^) id,innername,innerparent,innertype,innermutex,innertext doctype=ontology source=human entitytype=category > $@

# relations:
# name domain range inverse notes dbpedia inv-dbpedia
ontology-relations.json: task5.5_ontology/relations.tsv buildJson.py
	grep -v "^#" $< | \
	awk 'BEGIN{FS=OFS="\t"}{ \
	id="relation-" $$1; ${IDIFY} \
	gsub(",",", category ",$$2); ${PYTHONIFY}$$2); ${IDLIKEIFY}$$2); \
	gsub(",",", category ",$$3); ${PYTHONIFY}$$3); ${IDLIKEIFY}$$3); \
	gsub("n/a","",$$4); gsub(",",", relation ",$$4); ${PYTHONIFY}$$4); ${IDLIKEIFY}$$4); \
	 ${PYTHONIFY}$$6);  ${PYTHONIFY}$$7); \
	if (length($$2)>0) { $$2 = "\"category-" $$2 "\""; } \
	if (length($$3)>0) { $$3 = "\"category-" $$3 "\""; } \
	if (length($$4)>0) { $$4 = "\"relation-" $$4 "\""; } \
	if (length($$6)>0) { $$6 = "\"" $$6 "\""; } \
	if (length($$7)>1) { $$7 = "\"" $$7 "\""; } \
	print id,$$1,"@python["$$2"]","@python["$$3"]","@python["$$4"]","@python["$$6"]","@python["$$7"]",$$5}' | tail -n +2 | tr -d '\r' | grep -v "No. Use" | \
	python $(word 2,$^) id,innername,innerarg1data,innerarg2data,innerinverse,innertype,innerinversetype,innertext doctype=ontology source=human entitytype=relation > $@

# human labels
LABELERS:=eli lam joey
CATLABELTASKS:=ann1 ann2
label-categories.json: $(foreach el,$(LABELERS),$(foreach t,$(CATLABELTASKS),label-categories-$(el)-$(t).json))
	cat $^ | sed '/^[[]/d; /^]/d; /[^,]$$/ s/$$/ ,/' | sed '1 s/^/[\n/; $$ s/,$$/\n]/' > $@
label-categories-%.json: task7_interAnnotatorAgreement/results/%.norm.tsv buildJson.py
	tr -d '\r' < $< | \
	awk 'BEGIN{FS=OFS="\t"}{ if (NF>=2) { \
	task="$*"; sub("-",",",task); $(PYTHONIFY)task); \
	id="label-$*-" $$1; \
	lst="category-" $$2; \
	for(i=3;i<=NF;i++) {sub("^  *","",$$i); sub("  *$$","",$$i); gsub("  *","-",$$i); \
	if (length($$i)>0) {lst = lst ",category-" $$i;}} \
	arg2=""; for(i=NF;i>=2 && length(arg2)==0; i--) { arg2=$$i; } arg2 = "category-"arg2; \
	${PYTHONIFY}lst); \
	if (length(task)>0) { task = "\"" task "\""; } \
	if (length(lst)>0) { lst = "\"" lst "\""; } \
	print id,"tag-" $$1,"@python["task"]",arg2,arg2,"@python["lst"]"; }}' | \
	python $(word 2,$^) id,innerarg1,innertext,innerarg2,innertype,innerarg2data doctype=label source=human entitytype=category > $@

label-relations.json: $(foreach el,$(LABELERS),label-relations-iaa-$(el).json)
	cat $^ | sed '/^[[]/d; /^]/d; /[^,]$$/ s/$$/ ,/' | sed '1 s/^/[\n/; $$ s/,$$/\n]/' > $@
label-relations-iaa-%.json: task7_interAnnotatorAgreement/results/%-rel.tsv buildJson.py
	tr -d '\r' < $< | \
	awk 'BEGIN{FS=OFS="\t"}{ \
	id = "label-iaa-$*-"$$4"-"$$6; \
	gsub("  *","-",$$3); gsub("  *","-",$$5); gsub("  *","-",$$7); \
	task = "\"" $$2 ",aug2016\""; ${PYTHONIFY}task); \
	print id,"@python["task"]",$$4,"category-"$$5,$$6,"category-"$$7,"relation-"$$3; }' | \
	tail -n +2 | \
	python $(word 2,$^) id,innertext,innerarg1,innerarg1data,innerarg2,innerarg2data,innertype doctype=label source=human entitytype=relation > $@

# system predictions (from ssl expt)
predictions-categories.json:
	true


predictions-relations.json:
	true